package dev.nyxane.mods.scalmyth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.nyxane.mods.scalmyth.blocks.MeshBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4f;

import java.util.*;

public class MeshBlockRenderer implements BlockEntityRenderer<MeshBlockEntity> {
    public static VertexBuffer error_mesh(MeshBlockEntity meshBlockEntity) {
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

    private VertexBuffer get_vb(MeshBlockEntity meshBlockEntity) {
        var model = meshBlockEntity.getModel();
        try (var meshData = model.buildMeshData(meshBlockEntity.getLevel(), meshBlockEntity.getBlockPos(), "default", meshBlockEntity.face_light)) {
            if (meshData != null) {
                var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);

                vb.bind();
                vb.upload(Objects.requireNonNull(meshData));

                return vb;
            }
        }

        return error_mesh(meshBlockEntity);
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
}
