package dev.nyxane.mods.scalmyth.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
@OnlyIn(Dist.CLIENT)
public abstract class TitlescreenMixin {

    private long animStartTime = -1L;

    private static final ResourceLocation ANIM_OVERLAY =
            ResourceLocation.fromNamespaceAndPath("scalmyth", "textures/gui/fade_overlay.png");

    private static final int FRAME_COUNT = 44;
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 1152;
    private static final int FRAME_TIME_MS = 63;
    private static final int FRAMES_PER_ROW = 5;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/TitleScreen;renderPanorama(Lnet/minecraft/client/gui/GuiGraphics;F)V"
            ),
            cancellable = true
    )
    private void onRenderAfterBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Your animation code here
        // This runs AFTER the vanilla background is drawn, but BEFORE UI elements
        int screenW = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenH = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        final long now = System.currentTimeMillis();
        if (animStartTime == -1L) animStartTime = now;

        long elapsedAnim = now - animStartTime;
        int totalAnimTime = FRAME_COUNT * FRAME_TIME_MS;

        if (elapsedAnim < totalAnimTime) {
            int currentFrame = (int)(elapsedAnim / FRAME_TIME_MS);

            int frameX = (currentFrame % FRAMES_PER_ROW) * FRAME_WIDTH;
            int frameY = (currentFrame / FRAMES_PER_ROW) * FRAME_HEIGHT;

            int sheetW = FRAME_WIDTH * FRAMES_PER_ROW;
            int sheetH = (int)Math.ceil((double)FRAME_COUNT / FRAMES_PER_ROW) * FRAME_HEIGHT;

            float scale = Math.max((float)screenW / FRAME_WIDTH, (float)screenH / FRAME_HEIGHT);
            int drawW = Math.round(FRAME_WIDTH * scale);
            int drawH = Math.round(FRAME_HEIGHT * scale);
            int drawX = (screenW - drawW) / 2;
            int drawY = (screenH - drawH) / 2;

            g.blit(
                    ANIM_OVERLAY,
                    drawX, drawY,
                    drawW, drawH,
                    frameX, frameY,
                    FRAME_WIDTH, FRAME_HEIGHT,
                    sheetW, sheetH
            );

            ci.cancel(); // cancel further rendering if you want full overlay control
        }
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "floatValue=2000.0"))
    private float longerFade(float original) {
        return FRAME_COUNT * FRAME_TIME_MS;
    }
}
