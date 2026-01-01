package dev.nyxane.mods.scalmyth.mixin;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @Inject(method = "calculateShape", at = @At(value = "RETURN"))
    private void lightWithOffset(BlockAndTintGetter level, BlockState state, BlockPos pos, int[] vertices, Direction direction, float[] shape, BitSet shapeFlags, CallbackInfo ci) {
        if (state.is(ModBlocks.ROOF_BLOCK)) {
            shapeFlags.set(0, true);
        }
    }
}
