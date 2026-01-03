package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLLightDirection;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(MutableQuadViewImpl.class)
public class Sodium$MutableQuadViewImpl implements SBLLightDirection {
    @Unique
    private Direction light_direction;

    @Override
    public Direction sbl$getLightDirection() {
        return light_direction;
    }

    @Override
    public void sbl$setLightDirection(Direction direction) {
        light_direction = direction;
    }

    @Inject(method = "fromVanilla(Lnet/minecraft/client/renderer/block/model/BakedQuad;Lnet/fabricmc/fabric/api/renderer/v1/material/RenderMaterial;Lnet/minecraft/core/Direction;)Lnet/caffeinemc/mods/sodium/client/render/frapi/mesh/MutableQuadViewImpl;", at = @At("HEAD"))
    private void sbl$fromVanilla(BakedQuad quad, net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial par2, Direction par3, CallbackInfoReturnable<net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView> cir) {
        if (quad instanceof SBLLightDirection sblQuad) {
            light_direction = sblQuad.sbl$getLightDirection();
        }
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void sbl$clear(CallbackInfo ci) {
        light_direction = null;
    }
}
