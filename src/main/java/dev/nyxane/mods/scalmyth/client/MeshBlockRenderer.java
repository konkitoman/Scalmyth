package dev.nyxane.mods.scalmyth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.MeshBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

public class MeshBlockRenderer implements BlockEntityRenderer<MeshBlockEntity> {
    private final HashMap<ResourceLocation, VertexBuffer> MODELS = new HashMap<>();

    private VertexBuffer get_vb(MeshBlockEntity meshBlockEntity) {
        if (MODELS.containsKey(meshBlockEntity.model_location)) return MODELS.get(meshBlockEntity.model_location);

        MeshData meshData = null;

        var mc = Minecraft.getInstance();
        var resource_manager = mc.getResourceManager();
        var resource = resource_manager.getResource(meshBlockEntity.model_location);
        if (resource.isPresent()) {
            try {
                var file = resource.get().open();
                var text = new String(file.readAllBytes());
                ScalmythAPI.LOGGER.info("Model was found");
                var model = new WavefrontOBJ(text);
                meshData = model.buildMeshData("default");
            } catch (Exception e) {
                ScalmythAPI.LOGGER.error(e.toString());
            }
        }

        if (meshData == null) {
            var v0 = new Vector4f(0.5f, 1, 0, 1);
            var v1 = new Vector4f(1, 0, 0, 1);
            var v2 = new Vector4f(0, 0, 0, 1);

            var tesselator = Tesselator.getInstance();
            var bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).build());
            bufferBuilder.addVertex(v0.x, v0.y, v0.z).setColor(0xffff00ff);
            bufferBuilder.addVertex(v1.x, v1.y, v1.z).setColor(0xffff00ff);
            bufferBuilder.addVertex(v2.x, v2.y, v2.z).setColor(0xffff00ff);
            meshData = bufferBuilder.build();
        }

        var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);

        vb.bind();
        vb.upload(Objects.requireNonNull(meshData));

        ScalmythAPI.LOGGER.info("Loaded Model: {}", meshBlockEntity.model_location);

        MODELS.put(meshBlockEntity.model_location, vb);

        return vb;
    }

    @Override
    public void render(MeshBlockEntity meshBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        var vp = get_vb(meshBlockEntity);
        vp.bind();

        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        var tex = Minecraft.getInstance().getTextureManager().getTexture(meshBlockEntity.texture);
        var tex_light = Minecraft.getInstance().getTextureManager().getTexture(meshBlockEntity.light_texture);
        RenderSystem.setShaderTexture(0, tex.getId());
        RenderSystem.setShaderTexture(1, 0);
        RenderSystem.setShaderTexture(2, tex_light.getId());
        vp.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose()), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());

        poseStack.popPose();
    }


    ///  [Reference](https://paulbourke.net/dataformats/obj/)
    public static class WavefrontOBJ {
        private HashMap<String, Obj> Groups = new HashMap<>();
        private String[] ActiveGropus = new String[1];


        public WavefrontOBJ(String source) throws Exception {
            ActiveGropus[0] = "default";

            var s = new Scanner(source);
            var iterator = source.lines().iterator();

            while (iterator.hasNext()) {
                var line = iterator.next();

                var args = line.split(" ");

                switch (args[0]) {
                    case "":
                    case "#":
                        continue;
                    case "g":
                        var names_count = args.length - 1;
                        if (names_count == 0) continue;

                        if (ActiveGropus.length < names_count) {
                            ActiveGropus = new String[names_count];
                        }
                        Arrays.fill(ActiveGropus, null);
                        System.arraycopy(args, 1, ActiveGropus, 0, names_count);
                        break;
                    case "o":
                        break;
                    case "v":
                        if (args.length != 4) throw new Exception("the v keyword needs to have 3 arguments!");

                        for (String group_name : ActiveGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            group.add_vertex(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                            Groups.put(group_name, group);
                        }
                        break;
                    case "vn":
                        if (args.length != 4) throw new Exception("the vn keyword needs tp have 3 arguments!");

                        for (String group_name : ActiveGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            group.add_normal(Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
                            Groups.put(group_name, group);
                        }
                        break;
                    case "vt":
                        if (!(args.length == 2 || args.length == 3 || args.length == 4))
                            throw new Exception("the vt keyword needs to have 1, 2 or 3 arguments!");

                        for (String group_name : ActiveGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            double u = Double.parseDouble(args[1]), v = 0, w = 0;
                            if (args.length > 2) v = Double.parseDouble(args[2]);
                            if (args.length > 3) w = Double.parseDouble(args[3]);
                            group.add_texture_vertex(u, v, w);
                            Groups.put(group_name, group);
                        }
                        break;
                    case "f":
                        if (args.length < 4) throw new Exception("the f keyword needs tp have at minimum 3 arguments!");

                        var elements = args.length - 1;
                        int[] vertices = new int[elements];
                        int[] normals = new int[elements];
                        int[] texture_vertices = new int[elements];

                        Arrays.fill(vertices, 0);
                        Arrays.fill(normals, 0);
                        Arrays.fill(texture_vertices, 0);

                        for (var i = 0; i < elements; i++) {
                            var segments = args[i + 1].split("/");
                            vertices[i] = Integer.parseUnsignedInt(segments[0]);
                            if (segments.length > 1 && !segments[1].isEmpty())
                                texture_vertices[i] = Integer.parseUnsignedInt(segments[1]);
                            if (segments.length > 2 && !segments[2].isEmpty())
                                normals[i] = Integer.parseUnsignedInt(segments[2]);
                        }

                        for (String group_name : ActiveGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            group.add_face(new Face(vertices, normals, texture_vertices));
                            Groups.put(group_name, group);
                        }

                        break;
                    default:
                        ScalmythAPI.LOGGER.info("Unknown or unimplemented keyword: {}", args[0]);
                }
            }
        }

        public MeshData buildMeshData(String group_name) throws IllegalAccessException, NoSuchFieldException {
            var group = Groups.get(group_name);

            var format = DefaultVertexFormat.NEW_ENTITY;
            assert (format.getVertexSize() == VERTEX_SIZE);
            var buffer = new ByteBufferBuilder(group.vertices.size() * VERTEX_SIZE);

            final int[] vertices = {0};

            var indices = new ByteBufferBuilder(0);
            int indices_count = 0;
            for (var face : group.faces) {
                if (face.vertices.length == 3) {
                    var ptr = indices.reserve(12);
                    var v0 = createVertex(group, buffer, vertices, face.vertices[0], face.texture_vertices[0], face.normals[0]);
                    var v1 = createVertex(group, buffer, vertices, face.vertices[1], face.texture_vertices[1], face.normals[1]);
                    var v2 = createVertex(group, buffer, vertices, face.vertices[2], face.texture_vertices[2], face.normals[2]);
                    MemoryUtil.memPutInt(ptr, v0);
                    MemoryUtil.memPutInt(ptr + 4, v1);
                    MemoryUtil.memPutInt(ptr + 8, v2);
                    indices_count += 3;
                }
                if (face.vertices.length == 4) {
                    var ptr = indices.reserve(24);
                    var v0 = createVertex(group, buffer, vertices, face.vertices[0], face.texture_vertices[0], face.normals[0]);
                    var v1 = createVertex(group, buffer, vertices, face.vertices[1], face.texture_vertices[1], face.normals[1]);
                    var v2 = createVertex(group, buffer, vertices, face.vertices[2], face.texture_vertices[2], face.normals[2]);
                    var v3 = createVertex(group, buffer, vertices, face.vertices[3], face.texture_vertices[3], face.normals[3]);
                    MemoryUtil.memPutInt(ptr, v0);
                    MemoryUtil.memPutInt(ptr + 4, v1);
                    MemoryUtil.memPutInt(ptr + 8, v2);
                    MemoryUtil.memPutInt(ptr + 12, v0);
                    MemoryUtil.memPutInt(ptr + 16, v2);
                    MemoryUtil.memPutInt(ptr + 20, v3);
                    indices_count += 6;
                }
            }

            var mesh = new MeshData(Objects.requireNonNull(buffer.build()), new MeshData.DrawState(format, group.vertices.size(), indices_count, VertexFormat.Mode.TRIANGLES, VertexFormat.IndexType.INT));
            var field$indexBuffer = MeshData.class.getDeclaredField("indexBuffer");
            field$indexBuffer.setAccessible(true);
            field$indexBuffer.set(mesh, indices.build());

            return mesh;
        }

        public static int VERTEX_SIZE = 36;

        private static int createVertex(Obj group, ByteBufferBuilder buffer, int[] i, int vertex_i, int texture_vertex_i, int normal_i) {
            var ret = i[0];
            var vertex = group.vertices.get(vertex_i - 1);
            var texture_vertex = new Vector3d(0, 0, 0);
            if (texture_vertex_i != 0)
                texture_vertex = group.texture_vertices.get(texture_vertex_i - 1);
            var normal = new Vector3d(0, 0, 0);
            if (normal_i != 0)
                normal = group.normals.get(normal_i - 1);

            long ptr = buffer.reserve(VERTEX_SIZE);
            // Position
            MemoryUtil.memPutFloat(ptr, (float) vertex.x);
            MemoryUtil.memPutFloat(ptr + 4, (float) vertex.y);
            MemoryUtil.memPutFloat(ptr + 8, (float) vertex.z);
            // Color
            MemoryUtil.memPutInt(ptr + 12, 0xffffffff);
            // UV0
            MemoryUtil.memPutFloat(ptr + 16, (float) texture_vertex.x);
            MemoryUtil.memPutFloat(ptr + 20, (float) texture_vertex.y);
            // UV1
            MemoryUtil.memPutShort(ptr + 24, (short) 0);
            MemoryUtil.memPutShort(ptr + 26, (short) 0);
            // UV2
            MemoryUtil.memPutShort(ptr + 28, (short) 0);
            MemoryUtil.memPutShort(ptr + 30, (short) 0);
            // Normal
            MemoryUtil.memPutByte(ptr + 32, normalIntValue((float) normal.x));
            MemoryUtil.memPutByte(ptr + 33, normalIntValue((float) normal.y));
            MemoryUtil.memPutByte(ptr + 34, normalIntValue((float) normal.z));
            // Padding
            MemoryUtil.memPutByte(ptr + 35, (byte) 0xff);
            i[0] += 1;
            return ret;
        }

        private static byte normalIntValue(float value) {
            return (byte) ((int) (Mth.clamp(value, -1.0F, 1.0F) * 127.0F) & 0xFF);
        }

        public static class Obj {
            private final ArrayList<Vector3d> vertices = new ArrayList<>();
            private final ArrayList<Vector3d> normals = new ArrayList<>();
            private final ArrayList<Vector3d> texture_vertices = new ArrayList<>();
            private final ArrayList<Face> faces = new ArrayList<>();

            private Obj() {
            }

            private void add_vertex(double x, double y, double z) {
                vertices.add(new Vector3d(x, y, z));
            }

            private void add_normal(double x, double y, double z) {
                normals.add(new Vector3d(x, y, z));
            }

            private void add_texture_vertex(double x, double y, double z) {
                texture_vertices.add(new Vector3d(x, y, z));
            }

            private void add_face(Face face) {
                faces.add(face);
            }
        }

        public record Face(int[] vertices, int[] normals, int[] texture_vertices) {
        }
    }
}
