package dev.nyxane.mods.scalmyth.extrastuff;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
public class ObjImporter {
    static class Vec3fLocal {
        float x, y, z;
        Vec3fLocal(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    static class Vec2fLocal {
        float u, v;
        Vec2fLocal(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }
    public static class Mesh {
        public final List < Vec3fLocal > positionList = new ArrayList < > ();
        public final List < Vec2fLocal > textureCoordinateList = new ArrayList < > ();
        public final List < Vec3fLocal > normalList = new ArrayList < > ();
        public final List < int[] > faceList = new ArrayList < > ();
        public final List < String > faceMaterialList = new ArrayList < > ();
        public float[] packedPositions;
        public float[] packedTextureCoordinates;
        public float[] packedNormals;
        public int[] indices;
        public void bake() {
            List < VertexKey > keyList = new ArrayList < > ();
            List < float[] > vertexPositions = new ArrayList < > ();
            List < float[] > vertexTextureCoordinates = new ArrayList < > ();
            List < float[] > vertexNormals = new ArrayList < > ();
            List < Integer > indexList = new ArrayList < > ();
            Map < VertexKey, Integer > keyMap = new HashMap < > ();
            for (int[] face: faceList) {
                int vertexCount = face.length / 3;
                for (int i = 0; i < vertexCount; i++) {
                    int pIndex = face[i * 3 + 0];
                    int tIndex = face[i * 3 + 1];
                    int nIndex = face[i * 3 + 2];
                    VertexKey key = new VertexKey(pIndex, tIndex, nIndex);
                    Integer mappedIndex = keyMap.get(key);
                    if (mappedIndex == null) {
                        mappedIndex = keyList.size();
                        keyList.add(key);
                        keyMap.put(key, mappedIndex);
                        if (pIndex >= 0 && pIndex < positionList.size()) {
                            Vec3fLocal pos = positionList.get(pIndex);
                            vertexPositions.add(new float[] {
                                    pos.x, pos.y, pos.z
                            });
                        } else {
                            vertexPositions.add(new float[] {
                                    0f, 0f, 0f
                            });
                        }
                        if (tIndex >= 0 && tIndex < textureCoordinateList.size()) {
                            Vec2fLocal tc = textureCoordinateList.get(tIndex);
                            vertexTextureCoordinates.add(new float[] {
                                    tc.u, tc.v
                            });
                        } else vertexTextureCoordinates.add(new float[] {
                                0f, 0f
                        });
                        if (nIndex >= 0 && nIndex < normalList.size()) {
                            Vec3fLocal no = normalList.get(nIndex);
                            vertexNormals.add(new float[] {
                                    no.x, no.y, no.z
                            });
                        } else vertexNormals.add(new float[] {
                                0f, 0f, 0f
                        });
                    }
                    indexList.add(mappedIndex);
                }
            }
            packedPositions = new float[vertexPositions.size() * 3];
            for (int i = 0; i < vertexPositions.size(); i++) {
                float[] p = vertexPositions.get(i);
                packedPositions[i * 3] = p[0];
                packedPositions[i * 3 + 1] = p[1];
                packedPositions[i * 3 + 2] = p[2];
            }
            packedTextureCoordinates = new float[vertexTextureCoordinates.size() * 2];
            for (int i = 0; i < vertexTextureCoordinates.size(); i++) {
                float[] t = vertexTextureCoordinates.get(i);
                packedTextureCoordinates[i * 2] = t[0];
                packedTextureCoordinates[i * 2 + 1] = t[1];
            }
            packedNormals = new float[vertexNormals.size() * 3];
            for (int i = 0; i < vertexNormals.size(); i++) {
                float[] n = vertexNormals.get(i);
                packedNormals[i * 3] = n[0];
                packedNormals[i * 3 + 1] = n[1];
                packedNormals[i * 3 + 2] = n[2];
            }
            indices = indexList.stream().mapToInt(Integer::intValue).toArray();
        }
        static class VertexKey {
            final int positionIndex;
            final int textureIndex;
            final int normalIndex;
            VertexKey(int p, int t, int n) {
                this.positionIndex = p;
                this.textureIndex = t;
                this.normalIndex = n;
            }
            public boolean equals(Object o) {
                if (!(o instanceof VertexKey)) return false;
                VertexKey v = (VertexKey) o;
                return positionIndex == v.positionIndex && textureIndex == v.textureIndex && normalIndex == v.normalIndex;
            }
            public int hashCode() {
                return (positionIndex * 73856093) ^ (textureIndex * 19349663) ^ (normalIndex * 83492791);
            }
        }
    }
    public static class Material {
        public String name;
        public String diffuseMap;
    }
    public static class ModelPackage {
        public Mesh mesh = new Mesh();
        public final Map < String, Material > materialMap = new HashMap < > ();
        public final Map < String, BufferedImage > materialTextures = new HashMap < > ();
    }
    public static class ModelGeometry {
        public final CopyOnWriteArrayList < InGameQuad > quads = new CopyOnWriteArrayList < > ();
        public final Map < String, Material > materialMap = new ConcurrentHashMap < > ();
        public final Map < String, BufferedImage > materialTextures = new ConcurrentHashMap < > ();
        public int faceCount() {
            return quads.size();
        }
    }
    public static class InGameQuad {
        public final float[] positions;
        public final float[] normals;
        public final float[] uvs;
        public final String materialName;
        public final float minX, minY, minZ;
        public final float maxX, maxY, maxZ;
        public final float cx, cy, cz;
        public InGameQuad(float[] positions, float[] normals, float[] uvs, String materialName) {
            this.positions = positions;
            this.normals = (normals == null) ? computeFaceNormalArrayTriAsQuad(positions) : normals;
            this.uvs = uvs;
            this.materialName = materialName;
            float _minX = Float.POSITIVE_INFINITY, _minY = Float.POSITIVE_INFINITY, _minZ = Float.POSITIVE_INFINITY;
            float _maxX = Float.NEGATIVE_INFINITY, _maxY = Float.NEGATIVE_INFINITY, _maxZ = Float.NEGATIVE_INFINITY;
            float _cx = 0f, _cy = 0f, _cz = 0f;
            for (int v = 0; v < 3; v++) {
                float x = positions[v * 3 + 0];
                float y = positions[v * 3 + 1];
                float z = positions[v * 3 + 2];
                _minX = Math.min(_minX, x);
                _minY = Math.min(_minY, y);
                _minZ = Math.min(_minZ, z);
                _maxX = Math.max(_maxX, x);
                _maxY = Math.max(_maxY, y);
                _maxZ = Math.max(_maxZ, z);
                _cx += x;
                _cy += y;
                _cz += z;
            }
            this.minX = _minX;
            this.minY = _minY;
            this.minZ = _minZ;
            this.maxX = _maxX;
            this.maxY = _maxY;
            this.maxZ = _maxZ;
            this.cx = _cx / 3f;
            this.cy = _cy / 3f;
            this.cz = _cz / 3f;
        }
        static float[] computeFaceNormalArrayTriAsQuad(float[] positions12) {
            float ax = positions12[3] - positions12[0];
            float ay = positions12[4] - positions12[1];
            float az = positions12[5] - positions12[2];
            float bx = positions12[6] - positions12[0];
            float by = positions12[7] - positions12[1];
            float bz = positions12[8] - positions12[2];
            float nx = ay * bz - az * by;
            float ny = az * bx - ax * bz;
            float nz = ax * by - ay * bx;
            float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (len < 1e-8f) {
                nx = 0;
                ny = 1;
                nz = 0;
                len = 1;
            }
            nx /= len;
            ny /= len;
            nz /= len;
            return new float[] {
                    nx,
                    ny,
                    nz,
                    nx,
                    ny,
                    nz,
                    nx,
                    ny,
                    nz,
                    nx,
                    ny,
                    nz
            };
        }
    }
    private static final Map < String, ModelGeometry > GEOMETRY_REGISTRY = new ConcurrentHashMap < > ();
    public static ModelPackage loadModel(String modelName) throws IOException {
        String[] candidatePaths = new String[] {
                "resources/assets/scalmyth/obj/" + modelName + "/" + modelName + ".obj", "resources/assets/scalmyth/obj/" + modelName + ".obj", "assets/scalmyth/obj/" + modelName + "/" + modelName + ".obj", "assets/scalmyth/obj/" + modelName + ".obj", "/" + "assets/scalmyth/obj/" + modelName + "/" + modelName + ".obj", "/" + "assets/scalmyth/obj/" + modelName + ".obj"
        };
        InputStream chosenStream = null;
        String chosenPath = null;
        for (String p: candidatePaths) {
            chosenStream = openStream(p ,ObjImporter.class);
            if (chosenStream != null) {
                chosenPath = p;
                break;
            }
        }
        if (chosenStream == null) throw new IOException("OBJ not found for model: " + modelName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(chosenStream, StandardCharsets.UTF_8));
        Mesh mesh = new Mesh();
        Map < String, Material > materials = new HashMap < > ();
        String materialBasePath = basePath(chosenPath);
        List < String > materialFiles = new ArrayList < > ();
        String line;
        String currentMaterial = null;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("mtllib ")) {
                String rest = line.substring(7).trim();
                for (String token: rest.split("\\s+")) materialFiles.add(token);
            } else if (line.startsWith("v ")) {
                String[] parts = splitWhitespace(line.substring(2));
                if (parts.length >= 3) {
                    mesh.positionList.add(new Vec3fLocal(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                }
            } else if (line.startsWith("vt ")) {
                String[] parts = splitWhitespace(line.substring(3));
                if (parts.length >= 1) {
                    mesh.textureCoordinateList.add(new Vec2fLocal(Float.parseFloat(parts[0]), Float.parseFloat(parts.length > 1 ? parts[1] : "0")));
                }
            } else if (line.startsWith("vn ")) {
                String[] parts = splitWhitespace(line.substring(3));
                if (parts.length >= 3) {
                    mesh.normalList.add(new Vec3fLocal(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                }
            } else if (line.startsWith("usemtl ")) {
                currentMaterial = line.substring(7).trim();
            } else if (line.startsWith("f ")) {
                String[] parts = splitWhitespace(line.substring(2));
                if (parts.length < 3) continue;
                int[] faceVertices = new int[parts.length * 3];
                boolean badFace = false;
                for (int i = 0; i < parts.length; i++) {
                    String token = parts[i];
                    String[] indices = token.split("/", -1);
                    int p = parseIndex(indices, 0, mesh.positionList.size());
                    int t = parseIndex(indices, 1, mesh.textureCoordinateList.size());
                    int n = parseIndex(indices, 2, mesh.normalList.size());
                    if (p < 0) {
                        badFace = true;
                        break;
                    }
                    faceVertices[i * 3 + 0] = p;
                    faceVertices[i * 3 + 1] = t;
                    faceVertices[i * 3 + 2] = n;
                }
                if (badFace) {
                    System.out.println("[ObjImporter] Skipping face referencing missing position: " + Arrays.toString(faceVertices));
                    continue;
                }
                if (parts.length == 3) {
                    mesh.faceList.add(faceVertices);
                    mesh.faceMaterialList.add(currentMaterial);
                } else {
                    List < int[] > tris = triangulateFace(faceVertices, mesh);
                    if (tris == null || tris.isEmpty()) {
                        for (int i = 1; i < parts.length - 1; i++) {
                            int[] triangle = new int[9];
                            triangle[0] = faceVertices[0];
                            triangle[1] = faceVertices[1];
                            triangle[2] = faceVertices[2];
                            triangle[3] = faceVertices[i * 3 + 0];
                            triangle[4] = faceVertices[i * 3 + 1];
                            triangle[5] = faceVertices[i * 3 + 2];
                            triangle[6] = faceVertices[(i + 1) * 3 + 0];
                            triangle[7] = faceVertices[(i + 1) * 3 + 1];
                            triangle[8] = faceVertices[(i + 1) * 3 + 2];
                            mesh.faceList.add(triangle);
                            mesh.faceMaterialList.add(currentMaterial);
                        }
                    } else {
                        for (int[] tri: tris) {
                            mesh.faceList.add(tri);
                            mesh.faceMaterialList.add(currentMaterial);
                        }
                    }
                }
            }
        }
        reader.close();
        for (String materialFile: materialFiles) {
            String materialPath = resolveRelative(materialBasePath, materialFile);
            InputStream materialInput = openStream(materialPath, ObjImporter.class);
            if (materialInput == null) continue;
            BufferedReader materialReader = new BufferedReader(new InputStreamReader(materialInput, StandardCharsets.UTF_8));
            Material currentMaterialObj = null;
            String materialLine;
            while ((materialLine = materialReader.readLine()) != null) {
                materialLine = materialLine.trim();
                if (materialLine.isEmpty() || materialLine.startsWith("#")) continue;
                if (materialLine.startsWith("newmtl ")) {
                    currentMaterialObj = new Material();
                    currentMaterialObj.name = materialLine.substring(7).trim();
                    materials.put(currentMaterialObj.name, currentMaterialObj);
                } else if (materialLine.startsWith("map_Kd ") && currentMaterialObj != null) {
                    currentMaterialObj.diffuseMap = materialLine.substring(7).trim();
                }
            }
            materialReader.close();
        }
        mesh.bake();
        ModelPackage modelPackage = new ModelPackage();
        modelPackage.mesh.positionList.addAll(mesh.positionList);
        modelPackage.mesh.textureCoordinateList.addAll(mesh.textureCoordinateList);
        modelPackage.mesh.normalList.addAll(mesh.normalList);
        modelPackage.mesh.packedPositions = mesh.packedPositions;
        modelPackage.mesh.packedNormals = mesh.packedNormals;
        modelPackage.mesh.packedTextureCoordinates = mesh.packedTextureCoordinates;
        modelPackage.mesh.indices = mesh.indices;
        modelPackage.mesh.faceList.addAll(mesh.faceList);
        modelPackage.mesh.faceMaterialList.addAll(mesh.faceMaterialList);
        modelPackage.materialMap.putAll(materials);
        for (Material material: materials.values()) {
            if (material.diffuseMap == null) continue;
            String texturePath = resolveRelative(materialBasePath, material.diffuseMap);
            InputStream textureInput = openStream(texturePath, ObjImporter.class);
            if (textureInput != null) {
                try {
                    BufferedImage image = ImageIO.read(textureInput);
                    if (image != null) modelPackage.materialTextures.put(material.name, image);
                } catch (IOException ignored) {}
            }
        }
        if (modelPackage.materialTextures.isEmpty()) {
            String fallbackTex = resolveRelative(materialBasePath, modelName + ".png");
            InputStream texIn = openStream(fallbackTex, ObjImporter.class);
            if (texIn != null) {
                try {
                    BufferedImage img = ImageIO.read(texIn);
                    if (img != null) modelPackage.materialTextures.put("default", img);
                } catch (IOException ignored) {}
            }
        }
        return modelPackage;
    }
    public static void placeModel(ServerLevel world, String modelName, BlockPos origin, float scale) {
        placeModel(world, modelName, origin, scale, 0f, 0f, 0f);
    }
    public static void placeModel(ServerLevel world, String modelName, BlockPos origin, float scale, float rotXDeg, float rotYDeg, float rotZDeg) {
        final float providedScale = scale;
        final float rX = rotXDeg, rY = rotYDeg, rZ = rotZDeg;
        new Thread(()->{
        try {
            ModelPackage modelPackage = loadModel(modelName);
            float usedScale = providedScale;
            if (Float.isNaN(usedScale) || Float.isInfinite(usedScale)) usedScale = 1.0f;
            if (usedScale < 0.1f) usedScale = 0.1f;
            ModelGeometry geom = buildModelGeometryAsQuads(modelPackage, usedScale, origin, rX, rY, rZ);
            String key = registryKeyFor(modelName, origin);
            GEOMETRY_REGISTRY.put(key, geom);
            MinecraftServer server = world.getServer();
            if (server != null) {
                final String msg = "Geometry stored as key: " + key + " (faces=" + geom.quads.size() + ")";
                server.execute(()-> System.out.println("[ObjImporter] " + msg));
                float finalUsedScale = usedScale;
                server.execute(()-> ObjImporterNetworking.sendPlaceModelToAllClients(modelName, origin.getX(), origin.getY(), origin.getZ(), finalUsedScale, rX, rY, rZ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        }).start();
    }
    public static ModelGeometry buildModelGeometryAsQuads(ModelPackage modelPackage, float scale, BlockPos origin, float rotXDeg, float rotYDeg, float rotZDeg) {
        ModelGeometry geom = new ModelGeometry();
        Mesh mesh = modelPackage.mesh;
        Quaternionf quat = new Quaternionf().rotateXYZ((float) Math.toRadians(rotXDeg), (float) Math.toRadians(rotYDeg), (float) Math.toRadians(rotZDeg));
        geom.materialMap.putAll(modelPackage.materialMap);
        geom.materialTextures.putAll(modelPackage.materialTextures);
        for (int fi = 0; fi < mesh.faceList.size(); fi++) {
            int[] face = mesh.faceList.get(fi);
            String matName = null;
            if (fi < mesh.faceMaterialList.size()) matName = mesh.faceMaterialList.get(fi);
            if (face.length < 9) continue;
            float[] positions3 = new float[9];
            float[] normals3 = new float[9];
            float[] uvs3 = new float[6];
            boolean hasNormals = false, hasUVs = false;
            for (int vi = 0; vi < 3; vi++) {
                int pIndex = face[vi * 3 + 0];
                int tIndex = face[vi * 3 + 1];
                int nIndex = face[vi * 3 + 2];
                Vec3fLocal posv = (pIndex >= 0 && pIndex < mesh.positionList.size()) ? mesh.positionList.get(pIndex) : new Vec3fLocal(0f, 0f, 0f);
                Vector3f local = new Vector3f(posv.x, posv.y, posv.z);
                local.mul(scale);
                quat.transform(local);
                local.add(origin.getX(), origin.getY(), origin.getZ());
                positions3[vi * 3 + 0] = local.x;
                positions3[vi * 3 + 1] = local.y;
                positions3[vi * 3 + 2] = local.z;
                if (nIndex >= 0 && nIndex < mesh.normalList.size()) {
                    Vec3fLocal nv = mesh.normalList.get(nIndex);
                    Vector3f nvec = new Vector3f(nv.x, nv.y, nv.z);
                    quat.transform(nvec);
                    nvec.normalize();
                    normals3[vi * 3 + 0] = nvec.x;
                    normals3[vi * 3 + 1] = nvec.y;
                    normals3[vi * 3 + 2] = nvec.z;
                    hasNormals = true;
                } else {
                    normals3[vi * 3 + 0] = 0f;
                    normals3[vi * 3 + 1] = 0f;
                    normals3[vi * 3 + 2] = 0f;
                }
                if (tIndex >= 0 && tIndex < mesh.textureCoordinateList.size()) {
                    Vec2fLocal tv = mesh.textureCoordinateList.get(tIndex);
                    uvs3[vi * 2 + 0] = tv.u;
                    uvs3[vi * 2 + 1] = tv.v;
                    hasUVs = true;
                } else {
                    uvs3[vi * 2 + 0] = 0f;
                    uvs3[vi * 2 + 1] = 0f;
                }
            }
            float[] faceNormal = computeGeomNormalFromPositions3(positions3);
            float[] finalNormals4 = new float[12];
            if (!hasNormals) {
                for (int vi = 0; vi < 4; vi++) {
                    finalNormals4[vi * 3 + 0] = faceNormal[0];
                    finalNormals4[vi * 3 + 1] = faceNormal[1];
                    finalNormals4[vi * 3 + 2] = faceNormal[2];
                }
            } else {
                for (int vi = 0; vi < 3; vi++) {
                    float nx = normals3[vi * 3 + 0];
                    float ny = normals3[vi * 3 + 1];
                    float nz = normals3[vi * 3 + 2];
                    float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                    if (len < 1e-6f) {
                        finalNormals4[vi * 3 + 0] = faceNormal[0];
                        finalNormals4[vi * 3 + 1] = faceNormal[1];
                        finalNormals4[vi * 3 + 2] = faceNormal[2];
                    } else {
                        finalNormals4[vi * 3 + 0] = nx / len;
                        finalNormals4[vi * 3 + 1] = ny / len;
                        finalNormals4[vi * 3 + 2] = nz / len;
                    }
                }
                finalNormals4[3 * 3 + 0] = finalNormals4[2 * 3 + 0];
                finalNormals4[3 * 3 + 1] = finalNormals4[2 * 3 + 1];
                finalNormals4[3 * 3 + 2] = finalNormals4[2 * 3 + 2];
            }
            float[] positions4 = new float[12];
            float[] uvs4 = new float[8];
            for (int vi = 0; vi < 3; vi++) {
                positions4[vi * 3 + 0] = positions3[vi * 3 + 0];
                positions4[vi * 3 + 1] = positions3[vi * 3 + 1];
                positions4[vi * 3 + 2] = positions3[vi * 3 + 2];
                uvs4[vi * 2 + 0] = uvs3[vi * 2 + 0];
                uvs4[vi * 2 + 1] = uvs3[vi * 2 + 1];
            }
            positions4[3 * 3 + 0] = positions3[2 * 3 + 0];
            positions4[3 * 3 + 1] = positions3[2 * 3 + 1];
            positions4[3 * 3 + 2] = positions3[2 * 3 + 2];
            uvs4[3 * 2 + 0] = uvs3[2 * 2 + 0];
            uvs4[3 * 2 + 1] = uvs3[2 * 2 + 1];
            InGameQuad q = new InGameQuad(positions4, finalNormals4, hasUVs ? uvs4 : null, matName);
            geom.quads.add(q);
        }
        System.out.println("[ObjImporter] buildModelGeometryAsQuads: quads=" + geom.quads.size());
        return geom;
    }
    private static int parseIndex(String[] indices, int idx, int size) {
        if (indices.length <= idx || indices[idx].isEmpty()) {
            return -1;
        }
        try {
            int i = Integer.parseInt(indices[idx]);
            if (i < 0) {
                i = size + i;
            } else {
                i = i - 1;
            }
            if (i < 0 || i >= size) {
                return -1;
            }
            return i;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    private static List < int[] > triangulateFace(int[] faceVertices, Mesh mesh) {
        int n = faceVertices.length / 3;
        if (n < 3) return Collections.emptyList();
        if (n == 3) {
            return Collections.singletonList(Arrays.copyOf(faceVertices, 9));
        }
        float nx = 0f, ny = 0f, nz = 0f;
        for (int i = 0; i < n; i++) {
            Vec3fLocal a = mesh.positionList.get(faceVertices[i * 3 + 0]);
            Vec3fLocal b = mesh.positionList.get(faceVertices[((i + 1) % n) * 3 + 0]);
            nx += (a.y - b.y) * (a.z + b.z);
            ny += (a.z - b.z) * (a.x + b.x);
            nz += (a.x - b.x) * (a.y + b.y);
        }
        float absNx = Math.abs(nx), absNy = Math.abs(ny), absNz = Math.abs(nz);
        int dropAxis = 2;
        if (absNx >= absNy && absNx >= absNz) dropAxis = 0;
        else if (absNy >= absNx && absNy >= absNz) dropAxis = 1;
        else dropAxis = 2;
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) {
            Vec3fLocal v = mesh.positionList.get(faceVertices[i * 3 + 0]);
            if (dropAxis == 0) {
                xs[i] = v.y;
                ys[i] = v.z;
            } else if (dropAxis == 1) {
                xs[i] = v.x;
                ys[i] = v.z;
            } else {
                xs[i] = v.x;
                ys[i] = v.y;
            }
        }
        double area2 = 0.0;
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area2 += xs[i] * ys[j] - xs[j] * ys[i];
        }
        boolean isCCW = area2 > 0.0;
        List < Integer > vertIdx = new ArrayList < > (n);
        for (int i = 0; i < n; i++) vertIdx.add(i);
        List < int[] > outTriangles = new ArrayList < > ();
        int guard = 0;
        while (vertIdx.size() > 3 && guard++ < n * n) {
            boolean clipped = false;
            int m = vertIdx.size();
            for (int vi = 0; vi < m; vi++) {
                int prev = vertIdx.get((vi + m - 1) % m);
                int cur = vertIdx.get(vi);
                int next = vertIdx.get((vi + 1) % m);
                double ax = xs[cur] - xs[prev];
                double ay = ys[cur] - ys[prev];
                double bx = xs[next] - xs[cur];
                double by = ys[next] - ys[cur];
                double cross = ax * by - ay * bx;
                boolean isConvex = isCCW ? (cross > 1e-12) : (cross < -1e-12);
                if (!isConvex) continue;
                boolean hasInside = false;
                for (int k = 0; k < m; k++) {
                    int idx = vertIdx.get(k);
                    if (idx == prev || idx == cur || idx == next) continue;
                    if (pointInTriangle(xs[idx], ys[idx], xs[prev], ys[prev], xs[cur], ys[cur], xs[next], ys[next])) {
                        hasInside = true;
                        break;
                    }
                }
                if (hasInside) continue;
                int[] tri = new int[9];
                tri[0] = faceVertices[prev * 3 + 0];
                tri[1] = faceVertices[prev * 3 + 1];
                tri[2] = faceVertices[prev * 3 + 2];
                tri[3] = faceVertices[cur * 3 + 0];
                tri[4] = faceVertices[cur * 3 + 1];
                tri[5] = faceVertices[cur * 3 + 2];
                tri[6] = faceVertices[next * 3 + 0];
                tri[7] = faceVertices[next * 3 + 1];
                tri[8] = faceVertices[next * 3 + 2];
                outTriangles.add(tri);
                vertIdx.remove(vi);
                clipped = true;
                break;
            }
            if (!clipped) break;
        }
        if (vertIdx.size() == 3) {
            int a = vertIdx.get(0), b = vertIdx.get(1), c = vertIdx.get(2);
            int[] tri = new int[9];
            tri[0] = faceVertices[a * 3 + 0];
            tri[1] = faceVertices[a * 3 + 1];
            tri[2] = faceVertices[a * 3 + 2];
            tri[3] = faceVertices[b * 3 + 0];
            tri[4] = faceVertices[b * 3 + 1];
            tri[5] = faceVertices[b * 3 + 2];
            tri[6] = faceVertices[c * 3 + 0];
            tri[7] = faceVertices[c * 3 + 1];
            tri[8] = faceVertices[c * 3 + 2];
            outTriangles.add(tri);
        }
        if (outTriangles.isEmpty()) return null;
        return outTriangles;
    }
    private static boolean pointInTriangle(double px, double py, double ax, double ay, double bx, double by, double cx, double cy) {
        double v0x = cx - ax, v0y = cy - ay;
        double v1x = bx - ax, v1y = by - ay;
        double v2x = px - ax, v2y = py - ay;
        double dot00 = v0x * v0x + v0y * v0y;
        double dot01 = v0x * v1x + v0y * v1y;
        double dot02 = v0x * v2x + v0y * v2y;
        double dot11 = v1x * v1x + v1y * v1y;
        double dot12 = v1x * v2x + v1y * v2y;
        double denom = dot00 * dot11 - dot01 * dot01;
        if (Math.abs(denom) < 1e-12) return false;
        double invDenom = 1.0 / denom;
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        return u >= -1e-9 && v >= -1e-9 && (u + v) <= 1.0 + 1e-9;
    }
    private static float[] computeGeomNormalFromPositions3(float[] positions) {
        float ax = positions[3] - positions[0];
        float ay = positions[4] - positions[1];
        float az = positions[5] - positions[2];
        float bx = positions[6] - positions[0];
        float by = positions[7] - positions[1];
        float bz = positions[8] - positions[2];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-8f) {
            return new float[] {
                    0f, 1f, 0f
            };
        }
        return new float[] {
                nx / len, ny / len, nz / len
        };
    }
    static InputStream openStream(String path, Class<?>anchor) {
        try {
            InputStream in = anchor.getResourceAsStream(path.startsWith("/") ? path : ("/" + path));
            if ( in != null) return in;
            File file = new File(path);
            if (file.exists()) return new FileInputStream(file);
        } catch (Exception ignored) {}
        return null;
    }
    static String basePath(String path) {
        int idx = path.lastIndexOf('/');
        if (idx < 0) return "";
        return path.substring(0, idx + 1);
    }
    static String resolveRelative(String base, String relative) {
        if (relative.startsWith("/")) return relative;
        return base + relative;
    }
    static String[] splitWhitespace(String s) {
        return Arrays.stream(s.trim().split("\\s+")).filter(t -> !t.isEmpty()).toArray(String[]::new);
    }
    private static int executePlace(CommandContext < CommandSourceStack > ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        float scale = 1.0f;
        float rotX = 0f, rotY = 0f, rotZ = 0f;
        try {
            if (ctx.getInput().contains("scale")) scale = FloatArgumentType.getFloat(ctx, "scale");
        } catch (Exception ignored) {}
        try {
            if (ctx.getInput().contains("rotX")) rotX = FloatArgumentType.getFloat(ctx, "rotX");
        } catch (Exception ignored) {}
        try {
            if (ctx.getInput().contains("rotY")) rotY = FloatArgumentType.getFloat(ctx, "rotY");
        } catch (Exception ignored) {}
        try {
            if (ctx.getInput().contains("rotZ")) rotZ = FloatArgumentType.getFloat(ctx, "rotZ");
        } catch (Exception ignored) {}
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();
        ServerLevel world = source.getLevel();
        placeModel(world, name, pos, scale, rotX, rotY, rotZ);
        String key = registryKeyFor(name, pos);
        float finalScale = scale;
        float finalRotX = rotX;
        float finalRotY = rotY;
        float finalRotZ = rotZ;
        source.sendSuccess(()-> Component.literal("Geometry build queued: " + key + " (scale=" + finalScale + ", rot=" + finalRotX + "," + finalRotY + "," + finalRotZ + ")"), false);
        return Command.SINGLE_SUCCESS;
    }
    public static void storeReceivedGeometry(String key, ModelGeometry geom) {
        GEOMETRY_REGISTRY.put(key, geom);
    }
    public static final Map < String, ResourceLocation > TEXTURE_LOC_MAP = new ConcurrentHashMap < > ();
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("scalmyth_place").then(Commands.argument("name", StringArgumentType.string()).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ObjImporter::executePlace).then(Commands.argument("scale", FloatArgumentType.floatArg()).executes(ObjImporter::executePlace).then(Commands.argument("rotX", FloatArgumentType.floatArg()).then(Commands.argument("rotY", FloatArgumentType.floatArg()).then(Commands.argument("rotZ", FloatArgumentType.floatArg()).executes(ObjImporter::executePlace))))))));
    }
    static String registryKeyFor(String modelName, BlockPos origin) {
        return modelName + "@" + origin.getX() + "," + origin.getY() + "," + origin.getZ();
    }

    @net.neoforged.fml.common.EventBusSubscriber(modid = "scalmyth", value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class ClientRenderer {
        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            PoseStack ps = event.getPoseStack();
            Camera camera = event.getCamera();
            MultiBufferSource.BufferSource bufSource = mc.renderBuffers().bufferSource();
            Vec3 camPos = camera.getPosition();
            ps.pushPose();
            ps.translate(-camPos.x, -camPos.y, -camPos.z);
            try {
                for (Map.Entry < String, ModelGeometry > e: GEOMETRY_REGISTRY.entrySet()) {
                    String key = e.getKey();
                    ModelGeometry geom = e.getValue();
                    if (geom == null) continue;
                    ensureTexturesRegisteredForGeometry(key, geom);
                    for (InGameQuad q: geom.quads) {
                        ResourceLocation texLoc = null;
                        if (q.materialName != null) {
                            String texKey = key + ":" + q.materialName;
                            texLoc = TEXTURE_LOC_MAP.get(texKey);
                        }
                        if (texLoc == null) {
                            texLoc = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
                        }
                        VertexConsumer consumer = bufSource.getBuffer(RenderType.entityCutout(texLoc));
                        PoseStack.Pose poseEntry = ps.last();
                        int light = LevelRenderer.getLightColor(mc.level, new BlockPos((int) q.cx, (int) q.cy, (int) q.cz));
                        for (int vid: new int[] {
                                0,
                                1,
                                2
                        }) {
                            int vi = vid;
                            float vx = q.positions[vi * 3 + 0];
                            float vy = q.positions[vi * 3 + 1];
                            float vz = q.positions[vi * 3 + 2];
                            float tu = 0f, tv = 0f;
                            if (q.uvs != null && (vi * 2 + 1) < q.uvs.length) {
                                tu = q.uvs[vi * 2 + 0];
                                tv = 1f - q.uvs[vi * 2 + 1];
                            }
                            float nx = 0f, ny = 1f, nz = 0f;
                            if (q.normals != null && (vi * 3 + 2) < q.normals.length) {
                                nx = q.normals[vi * 3 + 0];
                                ny = q.normals[vi * 3 + 1];
                                nz = q.normals[vi * 3 + 2];
                            }
                            addFullVertex(consumer, poseEntry, vx, vy, vz, FastColor.ARGB32.color(255, 255, 255, 255), tu, tv, OverlayTexture.NO_OVERLAY, light, nx, ny, nz);
                        }
                        for (int vid: new int[] {
                                2,
                                3,
                                0
                        }) {
                            int vi = vid;
                            float vx = q.positions[vi * 3 + 0];
                            float vy = q.positions[vi * 3 + 1];
                            float vz = q.positions[vi * 3 + 2];
                            float tu = 0f, tv = 0f;
                            if (q.uvs != null && (vi * 2 + 1) < q.uvs.length) {
                                tu = q.uvs[vi * 2 + 0];
                                tv = 1f - q.uvs[vi * 2 + 1];
                            }
                            float nx = 0f, ny = 1f, nz = 0f;
                            if (q.normals != null && (vi * 3 + 2) < q.normals.length) {
                                nx = q.normals[vi * 3 + 0];
                                ny = q.normals[vi * 3 + 1];
                                nz = q.normals[vi * 3 + 2];
                            }
                            addFullVertex(consumer, poseEntry, vx, vy, vz, FastColor.ARGB32.color(255, 255, 255, 255), tu, tv, OverlayTexture.NO_OVERLAY, light, nx, ny, nz);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                ps.popPose();
                bufSource.endBatch();
            }
        }
        static void addFullVertex(VertexConsumer consumer, PoseStack.Pose pose, float vx, float vy, float vz, int color, float u, float v, int packedOverlay, int packedLight, float nx, float ny, float nz) {
            Vector3f pos = pose.pose().transformPosition(vx, vy, vz, new Vector3f());
            Vector3f normal = pose.transformNormal(nx, ny, nz, new Vector3f());
            consumer.addVertex(pos.x(), pos.y(), pos.z(), color, u, v, packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
        }
        private static void ensureTexturesRegisteredForGeometry(String registryKey, ModelGeometry geom) {
            if (geom == null || geom.materialTextures == null || geom.materialTextures.isEmpty()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getTextureManager() == null) return;
            for (Map.Entry < String, BufferedImage > e: geom.materialTextures.entrySet()) {
                String matName = e.getKey();
                BufferedImage img = e.getValue();
                if (img == null) continue;
                String mapKey = registryKey + ":" + matName;
                if (TEXTURE_LOC_MAP.containsKey(mapKey)) continue;
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "PNG", baos);
                    baos.flush();
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    com.mojang.blaze3d.platform.NativeImage nativeImg = com.mojang.blaze3d.platform.NativeImage.read(bais);
                    DynamicTexture dynTex = new DynamicTexture(nativeImg);
                    String safeMapKey = (registryKey + "_" + matName).toLowerCase().replaceAll("[^a-z0-9_\\.\\-]", "_");
                    String dynName = "scalmyth/" + safeMapKey;
                    ResourceLocation rl = ResourceLocation.parse(dynName);
                    mc.getTextureManager().register(rl, dynTex);
                    TEXTURE_LOC_MAP.put(mapKey, rl);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}