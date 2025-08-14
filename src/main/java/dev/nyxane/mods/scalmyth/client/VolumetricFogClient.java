package dev.nyxane.mods.scalmyth.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Objects;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class VolumetricFogClient {
    private static final String MODID = "scalmyth";
    private static final ResourceLocation PROGRAM_LOC = ResourceLocation.fromNamespaceAndPath(MODID, "program/volumetric_fog");

    private static ShaderInstance fogShader = null;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance instance = new ShaderInstance(event.getResourceProvider(), PROGRAM_LOC, DefaultVertexFormat.POSITION);
            event.registerShader(instance, shaderInstance -> fogShader = shaderInstance);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT)
    public static class RenderHandler {
        @SubscribeEvent
        public static void onRenderLast(RenderLevelStageEvent evt) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || fogShader == null) return;

            float partial = evt.getPartialTick().getGameTimeDeltaTicks();
            double camX = mc.gameRenderer.getMainCamera().getPosition().x;
            double camY = mc.gameRenderer.getMainCamera().getPosition().y;
            double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

            fogShader.apply();

            setUniform("GameTime", (float) (mc.level.getGameTime() + partial));
            setUniform("cameraPos", (float) camX, (float) camY, (float) camZ);
            setUniform("zNear", 0.05f);
            setUniform("zFar", 512.0f);

            setUniform("zFar", 512.0f);

            setUniform("lightDir", 0.0f, -1.0f, 0.0f);
            setUniform("lightColor", 1.0f, 1.0f, 1.0f);
            setUniform("lightIntensity", 2.0f);

            setUniform("fogAlbedo", 0.7f, 0.75f, 0.85f);
            setUniform("density", 0.04f);
            setUniform("anisotropy", 0.7f);

            setUniform("fogBaseHeight", 64.0f);
            setUniform("heightFalloff", 0.5f);

            setUniform("noiseScale", 0.02f);
            setUniform("noiseSpeed", 0.5f);
            setUniform("noiseAmplitude", 0.5f);

            setUniform("maxFogDistance", 200.0f);

            drawFullScreenQuad();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
        }

        private static void setUniform(String name, float... values) {
            try {
                var u = fogShader.getUniform(name);
                if (u != null) {
                    switch (values.length) {
                        case 1 -> u.set(values[0]);
                        case 2 -> u.set(values[0], values[1]);
                        case 3 -> u.set(values[0], values[1], values[2]);
                        case 4 -> u.set(values[0], values[1], values[2], values[3]);
                    }
                }
            } catch (Throwable ignored) {}
        }

        private static void drawFullScreenQuad() {
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder bb = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

            RenderSystem.disableDepthTest();

            bb.addVertex(-1.0F, -1.0F, -0.2F);
            bb.addVertex( 1.0F, -1.0F, -0.2F);
            bb.addVertex( 1.0F,  1.0F, -0.2F);
            bb.addVertex(-1.0F,  1.0F, -0.2F);

            BufferUploader.drawWithShader(Objects.requireNonNull(bb.build()));
            RenderSystem.enableDepthTest();
        }
    }
}
