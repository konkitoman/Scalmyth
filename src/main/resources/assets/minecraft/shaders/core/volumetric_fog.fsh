#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D tex; // The rendered scene texture
varying vec2 uv;       // UV coordinates

void main() {
    vec3 color = texture2D(tex, uv).rgb;

    // Darken everything almost completely
    color *= 0.01; // Really dark, nearly black

    gl_FragColor = vec4(color, 1.0);
}
