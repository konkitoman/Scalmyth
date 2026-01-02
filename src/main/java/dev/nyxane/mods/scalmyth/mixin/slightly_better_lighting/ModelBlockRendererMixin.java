package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import com.llamalad7.mixinextras.sugar.Local;
import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLBakedQuad;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @ModifyArg(method = "renderModelFaceFlat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I"), index = 2)
    public BlockPos renderModelFaceFlat(BlockPos pPos, @Local(name = "level") BlockAndTintGetter level, @Local(name = "pos") BlockPos pos, @Local(name = "bakedquad") BakedQuad bakedquad, @Local(name = "blockpos") BlockPos light_from) {
        if (bakedquad instanceof SBLBakedQuad quad) {
            var light_direction = quad.sbl$getLightDirection();
            if (light_direction == null) return pPos;
            return pos.relative(light_direction);
        }
        return pPos;
    }
}
