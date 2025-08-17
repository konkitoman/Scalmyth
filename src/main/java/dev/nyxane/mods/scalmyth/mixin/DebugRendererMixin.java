package dev.nyxane.mods.scalmyth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugRenderer.class)
public class DebugRendererMixin {

  @Final
  @Shadow
  public PathfindingRenderer pathfindingRenderer;

  @Inject(method = "render", at = @At("HEAD"))
  public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo cir){
      if (Minecraft.getInstance().getDebugOverlay().showDebugScreen()){
          pathfindingRenderer.render(poseStack, bufferSource, camX, camY, camZ);
      }
  }
}
