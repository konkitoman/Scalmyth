package dev.nyxane.mods.scalmyth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.nyxane.mods.scalmyth.blocks.MeshBlockEntity;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Objects;

public class MeshBlockRenderer implements BlockEntityRenderer<MeshBlockEntity> {
    public static VertexBuffer VERTEX_BUFFER;

    private VertexBuffer get_vb(){
        if (VERTEX_BUFFER != null) return VERTEX_BUFFER;
        var v0 = new Vector4f(0.5f, 1, 0, 1);
        var v1 = new Vector4f(1, 0, 0, 1);
        var v2 = new Vector4f(0, 0, 0, 1);

        var tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.begin(VertexFormat.Mode.TRIANGLES, VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).build());
        bufferBuilder.addVertex( v0.x, v0.y, v0.z).setColor(0xffff0000);
        bufferBuilder.addVertex( v1.x, v1.y, v1.z).setColor(0xffff0000);
        bufferBuilder.addVertex( v2.x, v2.y, v2.z).setColor(0xffff0000);

        var vb = new VertexBuffer(VertexBuffer.Usage.STATIC);

        vb.bind();
        vb.upload(Objects.requireNonNull(bufferBuilder.build()));

        VERTEX_BUFFER = vb;

        return vb;
    }

    @Override
    public void render(MeshBlockEntity meshBlockEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        var vp = get_vb();
        vp.bind();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        vp.drawWithShader(new Matrix4f(RenderSystem.getModelViewMatrix()).mul(poseStack.last().pose()), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
    }
}
