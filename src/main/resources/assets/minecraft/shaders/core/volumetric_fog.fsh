#version 330 core
// ─────────────────────────────────────────────────────────────────────────────
// Volumetric Fog (single scattering) — GLSL 330
// - Raymarch to scene depth (from DepthSampler) for proper thickness
// - Height fog with exponential falloff
// - Animated fractal 3D noise to shape fog density (winded over time)
// - Henyey–Greenstein phase approximation for forward-scattering godrays
// - Blue-noise / Bayer dither to reduce banding & marching rings
// - Quality knobs & early-outs for performance
// ─────────────────────────────────────────────────────────────────────────────

// ------------------------------- Quality -------------------------------------
#define VOL_STEPS          48     // Primary raymarch steps (16–96 typical)
#define VOL_STEPS_SHADOW   12     // Shadow ray steps (0 to disable)
#define JITTER_STRENGTH    1.0    // 0..1 amount of stochastic jitter
#define NOISE_OCTAVES      3      // FBM octaves (1–5)
#define ENABLE_HEIGHT_FOG  1
#define ENABLE_NOISE       1
#define ENABLE_SHADOWS     0      // Set to 1 if you wire a shadow map

// ----------------------------- I/O & Uniforms --------------------------------
in vec4 gl_FragCoord;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;     // color buffer
uniform sampler2D DepthSampler;       // scene depth (nonlinear)
#if ENABLE_SHADOWS
uniform sampler2DShadow ShadowMap;    // optional shadowmap
uniform mat4 lightViewProj;           // light VP for shadow lookups
#endif

// Matrices to reconstruct world positions from depth
uniform mat4 invProjection;
uniform mat4 invView;
uniform mat4 view;                    // for view-space things if you need

// Camera & time
uniform vec3 cameraPos;
uniform float GameTime;               // seconds

// Camera frustum
uniform float zNear;                  // camera near
uniform float zFar;                   // camera far

// Lighting (directional)
uniform vec3 lightDir;                // normalized world-space direction TOWARD the light (e.g. sun)
uniform vec3 lightColor;              // RGB light color (intensity baked in or use lightIntensity)
uniform float lightIntensity;         // scalar intensity multiplier

// Fog appearance
uniform vec3 fogAlbedo;               // base fog color (scattering albedo)
uniform float density;                // base extinction density (0.0–1.0 typical ~0.02)
uniform float anisotropy;             // HG phase g (-0.2 backscatter .. 0.9 forward); try 0.6–0.85

// Height fog (if enabled)
uniform float fogBaseHeight;          // world height where fog is densest
uniform float heightFalloff;          // >0 falloff; 0.1 .. 2.0 (bigger -> quicker fade with altitude)

// Noise shaping (if enabled)
uniform float noiseScale;             // world scale of noise lobes, e.g. 0.02
uniform float noiseSpeed;             // world wind speed for noise, e.g. 0.5
uniform float noiseAmplitude;         // 0..1 extra density from noise, e.g. 0.5

// March distance cap (safety)
uniform float maxFogDistance;         // hard cap in meters (e.g. 200.0)

// ------------------------------ Helpers --------------------------------------
// 4x4 Bayer matrix for cheap dithering (helps banding)
float bayer4(in ivec2 p) {
    int x = p.x & 3;
    int y = p.y & 3;
    int m[16] = int[16](
    0,  8,  2, 10,
    12,  4, 14,  6,
    3, 11,  1,  9,
    15,  7, 13,  5
    );
    return float(m[y*4 + x]) / 16.0;
}

// Hash & value noise
float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float mynoise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    // Smoothstep
    vec3 u = f*f*(3.0 - 2.0*f);

    float n000 = hash(i + vec3(0,0,0));
    float n100 = hash(i + vec3(1,0,0));
    float n010 = hash(i + vec3(0,1,0));
    float n110 = hash(i + vec3(1,1,0));
    float n001 = hash(i + vec3(0,0,1));
    float n101 = hash(i + vec3(1,0,1));
    float n011 = hash(i + vec3(0,1,1));
    float n111 = hash(i + vec3(1,1,1));

    float nx00 = mix(n000, n100, u.x);
    float nx10 = mix(n010, n110, u.x);
    float nx01 = mix(n001, n101, u.x);
    float nx11 = mix(n011, n111, u.x);

    float nxy0 = mix(nx00, nx10, u.y);
    float nxy1 = mix(nx01, nx11, u.y);

    return mix(nxy0, nxy1, u.z);
}

float fbm(vec3 p) {
    float a = 0.5;
    float f = 0.0;
    for (int o = 0; o < NOISE_OCTAVES; ++o) {
        f += a * mynoise3(p);
        p *= 2.0;
        a *= 0.5;
    }
    return f;
}

// Approximate Henyey–Greenstein phase function (normalized-ish)
float phaseHG(float cosTheta, float g) {
    float g2 = g * g;
    float denom = pow(1.0 + g2 - 2.0 * g * cosTheta, 1.5);
    return (1.0 - g2) / max(0.0001, (4.0 * 3.14159265 * denom));
}

// Depth helpers
float linearizeDepth(float depthNDC) {
    // depthNDC is sampled from typical GL depth buffer in [0..1] non-linear
    // Convert to view-space z (positive forward) then to linear [0..zFar]
    float z = depthNDC * 2.0 - 1.0;                 // back to NDC [-1..1]
    return (2.0 * zNear * zFar) / (zFar + zNear - z * (zFar - zNear));
}

// Reconstruct world-space position from depth (camera ray)
vec3 worldPosFromDepth(vec2 uv, float depth01) {
    // Build clip space position
    vec4 clip = vec4(uv * 2.0 - 1.0, depth01 * 2.0 - 1.0, 1.0);
    vec4 viewPos = invProjection * clip;
    viewPos /= viewPos.w;
    vec4 worldPos = invView * viewPos;
    return worldPos.xyz;
}

// Shadow (optional)
#if ENABLE_SHADOWS
float shadowAttenuation(vec3 worldPos) {
    vec4 lp = lightViewProj * vec4(worldPos, 1.0);
    lp.xyz /= lp.w;
    vec3 uvw = lp.xyz * 0.5 + 0.5;
    if (uvw.x < 0.0 || uvw.x > 1.0 || uvw.y < 0.0 || uvw.y > 1.0 || uvw.z < 0.0 || uvw.z > 1.0)
    return 1.0;
    // PCF could be added here; keep single sample for cost
    return texture(ShadowMap, uvw);
}
#endif

// Density field: base * height falloff * (1 + noise)
float sampleDensity(vec3 pWorld) {
    float d = density;

    #if ENABLE_HEIGHT_FOG
    float h = pWorld.y - fogBaseHeight;
    // Exponential falloff above base height, clamp to avoid inf
    d *= exp(-heightFalloff * max(0.0, h));
    #endif

    #if ENABLE_NOISE
    vec3 wind = vec3(lightDir.x, 0.0, lightDir.z);
    vec3 np = pWorld * noiseScale + wind * (noiseSpeed * GameTime);
    float n = fbm(np);                  // 0..~1
    // Center around 0 then apply amplitude
    d *= (1.0 + noiseAmplitude * (n * 2.0 - 1.0));
    #endif

    return max(d, 0.0);
}

// ------------------------------ Main -----------------------------------------
void main() {
    vec2 resolution = vec2(textureSize(DiffuseSampler, 0));
    vec2 uv = gl_FragCoord.xy / resolution;

    vec4 scene = texture(DiffuseSampler, uv);

    // If no depth is bound, just return scene
    if (textureSize(DepthSampler, 0).x == 0) {
        fragColor = scene;
        return;
    }

    float depth01 = texture(DepthSampler, uv).r;
    if (depth01 >= 1.0) {
        // Sky pixel: march up to maxFogDistance cap
        // fabricate an end point far away in view direction
    }

    // Reconstruct endpoints
    vec3 P0 = cameraPos;                             // world camera
    vec3 Pw = worldPosFromDepth(uv, depth01);        // world position at scene depth
    float sceneDist = distance(P0, Pw);
    if (maxFogDistance > 0.0) sceneDist = min(sceneDist, maxFogDistance);

    // Early out if virtually nothing to march
    if (sceneDist <= 0.001) {
        fragColor = scene;
        return;
    }

    // Marching setup
    int steps = VOL_STEPS;
    float stepLen = sceneDist / float(steps);

    // Pixel jitter (dither) to break up banding/rings
    float bayer = bayer4(ivec2(gl_FragCoord.xy));
    float jitter = (bayer - 0.5) * JITTER_STRENGTH;
    float t = stepLen * (0.5 + jitter); // start slightly inside

    vec3 dir = normalize(Pw - P0);

    // Accumulators
    vec3 L = vec3(0.0);    // in-scattered radiance
    float Tr = 1.0;        // transmittance (Beer-Lambert)

    // Precompute
    float g = clamp(anisotropy, -0.95, 0.95);
    float mu = dot(dir, lightDir);                   // cos(theta) between view ray and light dir
    float phase = phaseHG(mu, g);

    for (int i = 0; i < VOL_STEPS; ++i) {
        if (t > sceneDist || Tr < 0.001) break;

        vec3 p = P0 + dir * t;

        float sigma_t = sampleDensity(p);            // extinction coeff
        float sigma_s = sigma_t;                     // single scattering (albedo~1; tint below)

        // Shadowing along the light direction (optional, cheap straight line)
        float lightVis = 1.0;
        #if ENABLE_SHADOWS
        if (VOL_STEPS_SHADOW > 0) {
            vec3 lp = p;
            float lStep = stepLen * 2.5;            // coarser than primary
            for (int s = 0; s < VOL_STEPS_SHADOW; ++s) {
                lp += lightDir * lStep;
                float sd = sampleDensity(lp);
                lightVis *= exp(-sd * lStep * 1.0);
                if (lightVis < 0.02) break;
            }
            // Combine with shadow map if provided
            lightVis *= shadowAttenuation(p);
        }
        #endif

        // Beer-Lambert for this segment
        float atten = exp(-sigma_t * stepLen);

        // In-scattered light contributed by this segment
        vec3 scatterColor = fogAlbedo * lightColor * lightIntensity;
        vec3 inscatter = scatterColor * sigma_s * phase * Tr * lightVis * stepLen;

        L += inscatter;
        Tr *= atten;

        t += stepLen;
    }

    // Fog transmittance applied to scene color (out-scattering), then add in-scatter
    vec3 outColor = scene.rgb * Tr + L;

    // Optional: clamp to avoid overbright
    outColor = clamp(outColor, 0.0, 1.0);

    fragColor = vec4(outColor, 1.0);
}
