package dev.nyxane.mods.scalmyth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.nyxane.mods.scalmyth.KDebug;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.MeshBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class MeshBlockRenderer implements BlockEntityRenderer<MeshBlockEntity> {
    private final HashMap<ResourceLocation, WavefrontOBJ> MODELS = new HashMap<>();

    private VertexBuffer get_vb(MeshBlockEntity meshBlockEntity) {
        if (MODELS.containsKey(meshBlockEntity.model_location)) {
            var model = MODELS.get(meshBlockEntity.model_location);
            try (var meshData = model.buildMeshData(meshBlockEntity.getLevel(), meshBlockEntity.getBlockPos(), "default", meshBlockEntity.face_light)) {
                if (meshData != null) {
                    var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);

                    vb.bind();
                    vb.upload(Objects.requireNonNull(meshData));

                    return vb;
                }
            }

            MODELS.remove(meshBlockEntity.model_location);
        }

        var mc = Minecraft.getInstance();
        var resource_manager = mc.getResourceManager();
        var resource = resource_manager.getResource(meshBlockEntity.model_location);
        if (resource.isPresent()) {
            try {
                var file = resource.get().open();
                var text = new String(file.readAllBytes());
                ScalmythAPI.LOGGER.info("Model was found");
                var model = new WavefrontOBJ(text);
                ScalmythAPI.LOGGER.info("Loaded Model: {}", meshBlockEntity.model_location);
                if (model.isSuccess()) {
                    MODELS.put(meshBlockEntity.model_location, model);
                    return get_vb(meshBlockEntity);
                }
            } catch (Exception e) {
                ScalmythAPI.LOGGER.error(e.toString());
            }
        }

        var tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
        var level = meshBlockEntity.getLevel();
        var pos = meshBlockEntity.getBlockPos();
        var light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));
        bufferBuilder.addVertex(-0.5f, -0.5f, -0.5f, 0xffff00ff, 0f, 0f, 0, light, 0, 0, -1);
        bufferBuilder.addVertex(0.0f, 0.5f, -0.5f, 0xffff00ff, 0f, 1f, 0, light, 0, 0, -1);
        bufferBuilder.addVertex(0.5f, -0.5f, -0.5f, 0xffff00ff, 1f, 0f, 0, light, 0, 0, -1);

        try (var meshData = bufferBuilder.build()) {
            var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);

            vb.bind();
            vb.upload(Objects.requireNonNull(meshData));

            return vb;
        }
    }

    @Override
    public void render(MeshBlockEntity meshBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int i1) {
        if (meshBlockEntity.stage == 0) return;

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);

        var mc = Minecraft.getInstance();

        try (var vp = get_vb(meshBlockEntity)) {
            vp.bind();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1, 1, 1, 1);
            var tex = mc.getTextureManager().getTexture(meshBlockEntity.texture);
            RenderSystem.setShaderTexture(0, tex.getId());
            RenderSystem.setShaderTexture(1, 0);
            mc.gameRenderer.lightTexture().turnOnLightLayer();
            vp.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose()), RenderSystem.getProjectionMatrix(), GameRenderer.getRendertypeEntitySolidShader());
            VertexBuffer.unbind();
        }

        poseStack.popPose();
    }


    ///  [Reference](https://paulbourke.net/dataformats/obj/)
    public static class WavefrontOBJ {
        private final HashMap<String, Obj> Groups = new HashMap<>();

        public WavefrontOBJ(String source) throws Exception {
            String[] activeGropus = new String[1];
            activeGropus[0] = "default";

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

                        if (activeGropus.length < names_count) {
                            activeGropus = new String[names_count];
                        }
                        Arrays.fill(activeGropus, null);
                        System.arraycopy(args, 1, activeGropus, 0, names_count);
                        break;
                    case "o":
                        break;
                    case "v":
                        if (args.length != 4) throw new Exception("the v keyword needs to have 3 arguments!");

                        for (String group_name : activeGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            group.add_vertex(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
                            Groups.put(group_name, group);
                        }
                        break;
                    case "vn":
                        if (args.length != 4) throw new Exception("the vn keyword needs tp have 3 arguments!");

                        for (String group_name : activeGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            group.add_normal(Float.parseFloat(args[1]), Float.parseFloat(args[2]), Float.parseFloat(args[3]));
                            Groups.put(group_name, group);
                        }
                        break;
                    case "vt":
                        if (!(args.length == 2 || args.length == 3 || args.length == 4))
                            throw new Exception("the vt keyword needs to have 1, 2 or 3 arguments!");

                        for (String group_name : activeGropus) {
                            if (group_name == null) break;

                            var group = Groups.getOrDefault(group_name, new Obj());
                            float u = Float.parseFloat(args[1]), v = 0, w = 0;
                            if (args.length > 2) v = Float.parseFloat(args[2]);
                            if (args.length > 3) w = Float.parseFloat(args[3]);
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

                        for (String group_name : activeGropus) {
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

        public boolean isSuccess() {
            return !Groups.isEmpty();
        }

        public MeshData buildMeshData(Level level, BlockPos origin, String group_name, boolean face_light) {
            var group = Groups.get(group_name);

            if (group == null) return null;

            var builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.NEW_ENTITY);
            for (var face : group.faces) {
                if (face.vertices.length == 3) {
                    int light = 1;

                    if (face_light) {
                        var p0 = group.vertices.get(face.vertices[0] - 1);
                        var p1 = group.vertices.get(face.vertices[1] - 1);
                        var p2 = group.vertices.get(face.vertices[2] - 1);

                        var n0 = group.normals.get(face.normals[0] - 1);
                        var n1 = group.normals.get(face.normals[1] - 1);
                        var n2 = group.normals.get(face.normals[2] - 1);

                        var o = new Vector3f(p0).add(p1).add(p2).div(3);
                        var O = new Vector3f(o).add(origin.getX(), origin.getY(), origin.getZ()).add(0.5f, 0.5f, 0.5f);
                        var n = new Vector3f(n0).add(n1).add(n2).div(3);
                        var up = new Vector3f(O).add(n.mul(0.5f));

                        var block_l = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(up.x, up.y, up.z));
                        var sky_l = level.getBrightness(LightLayer.SKY, BlockPos.containing(up.x, up.y, up.z));
                        light = LightTexture.pack(block_l, sky_l);

                        KDebug.addShape(level, new KDebug.Shape.Lines(new Vec3(O.x, O.y, O.z), new Vec3(up.x, up.y, up.z), 0xff00ff00).setId(List.of(level, O, up)));
                    }

                    createVertex(level, origin, group, builder, face.vertices[0], face.texture_vertices[0], face.normals[0], light);
                    createVertex(level, origin, group, builder, face.vertices[1], face.texture_vertices[1], face.normals[1], light);
                    createVertex(level, origin, group, builder, face.vertices[2], face.texture_vertices[2], face.normals[2], light);
                }
                if (face.vertices.length == 4) {
                    int light = 1;

                    if (face_light) {
                        var p0 = group.vertices.get(face.vertices[0] - 1);
                        var p1 = group.vertices.get(face.vertices[1] - 1);
                        var p2 = group.vertices.get(face.vertices[2] - 1);
                        var p3 = group.vertices.get(face.vertices[3] - 1);

                        var n0 = group.normals.get(face.normals[0] - 1);
                        var n1 = group.normals.get(face.normals[1] - 1);
                        var n2 = group.normals.get(face.normals[2] - 1);
                        var n3 = group.normals.get(face.normals[3] - 1);

                        var o = new Vector3f(p0).add(p1).add(p2).add(p3).div(4);
                        var O = new Vector3f(o).add(origin.getX(), origin.getY(), origin.getZ()).add(0.5f, 0.5f, 0.5f);
                        var n = new Vector3f(n0).add(n1).add(n2).add(n3).div(4);
                        var up = new Vector3f(O).add(n.mul(0.5f));

                        var block_l = level.getBrightness(LightLayer.BLOCK, BlockPos.containing(up.x, up.y, up.z));
                        var sky_l = level.getBrightness(LightLayer.SKY, BlockPos.containing(up.x, up.y, up.z));
                        light = LightTexture.pack(block_l, sky_l);

                        KDebug.addShape(level, new KDebug.Shape.Lines(new Vec3(O.x, O.y, O.z), new Vec3(up.x, up.y, up.z), 0xff00ff00).setId(List.of(level, O, up)));
                    }

                    createVertex(level, origin, group, builder, face.vertices[0], face.texture_vertices[0], face.normals[0], light);
                    createVertex(level, origin, group, builder, face.vertices[1], face.texture_vertices[1], face.normals[1], light);
                    createVertex(level, origin, group, builder, face.vertices[2], face.texture_vertices[2], face.normals[2], light);
                    createVertex(level, origin, group, builder, face.vertices[0], face.texture_vertices[0], face.normals[0], light);
                    createVertex(level, origin, group, builder, face.vertices[2], face.texture_vertices[2], face.normals[2], light);
                    createVertex(level, origin, group, builder, face.vertices[3], face.texture_vertices[3], face.normals[3], light);
                }
            }

            return builder.build();
        }

        private static final List<Vec3> SAMPLE_POINTS = List.of(
            new Vec3(0.5, 0.5, 0), new Vec3(-0.5, 0.5, 0), new Vec3(0.5, -0.5, 0), new Vec3(-0.5, -0.5, 0),
            new Vec3(0, 0.5, 0.5), new Vec3(0, -0.5, 0.5), new Vec3(0, 0.5, -0.5), new Vec3(0, -0.5, -0.5),
            new Vec3(0.5, 0, 0.5), new Vec3(-0.5, 0, 0.5), new Vec3(0.5, 0, -0.5), new Vec3(-0.5, 0, -0.5)
        );

        private static void createVertex(Level level, BlockPos origin, Obj group, BufferBuilder builder, int vertex_i, int texture_vertex_i, int normal_i, int light) {
            var vertex = group.vertices.get(vertex_i - 1);
            var texture_vertex = new Vector3f(0, 0, 0);
            if (texture_vertex_i != 0) texture_vertex = group.texture_vertices.get(texture_vertex_i - 1);
            var normal = new Vector3f(0, 0, 0);
            if (normal_i != 0) normal = group.normals.get(normal_i - 1);

            if (light == 1) {
                var pos = new Vec3(vertex.x, vertex.y, vertex.z).add(Vec3.atCenterOf(origin));

                var n = new Vec3(normal.x, normal.y, normal.z);
                var to_pos = pos.add(n.scale(0.5));

                KDebug.addShape(level, new KDebug.Shape.Lines(pos, to_pos, 0xff0000ff).setId(List.of(level, pos, to_pos)));

                int block = 0, sky = 0;
                var i = 0;
                for (var dir : SAMPLE_POINTS) {
                    if (Math.abs(dir.dot(n)) > 0.25) continue;
                    var d = n.cross(dir).scale(0.5);
                    var s = pos.add(d);
                    var e = s.add(n.scale(0.25));
                    KDebug.addShape(level, new KDebug.Shape.Lines(s, e, 0xffff00ff).setId(List.of(level, dir, s, e)));
                    var block_pos = BlockPos.containing(e.x, e.y, e.z);
                    block += level.getBrightness(LightLayer.BLOCK, block_pos);
                    sky += level.getBrightness(LightLayer.SKY, block_pos);
                    i += 1;
                }

                light = LightTexture.pack(block / Math.max(i, 1), sky / Math.max(i, 1));
            }

            builder.addVertex(vertex.x, vertex.y, vertex.z, 0xffffffff, texture_vertex.x, 0 - texture_vertex.y, 0, light, normal.x, normal.y, normal.z);
        }

        public static class Obj {
            private final ArrayList<Vector3f> vertices = new ArrayList<>();
            private final ArrayList<Vector3f> normals = new ArrayList<>();
            private final ArrayList<Vector3f> texture_vertices = new ArrayList<>();
            private final ArrayList<Face> faces = new ArrayList<>();

            private Obj() {
            }

            private void add_vertex(float x, float y, float z) {
                vertices.add(new Vector3f(x, y, z));
            }

            private void add_normal(float x, float y, float z) {
                normals.add(new Vector3f(x, y, z));
            }

            private void add_texture_vertex(float x, float y, float z) {
                texture_vertices.add(new Vector3f(x, y, z));
            }

            private void add_face(Face face) {
                faces.add(face);
            }
        }

        public record Face(int[] vertices, int[] normals, int[] texture_vertices) {
        }
    }
}
