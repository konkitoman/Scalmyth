package dev.nyxane.mods.scalmyth.mixin;

import dev.nyxane.mods.scalmyth.registry.Blocks;
import io.netty.util.Attribute;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

@Mixin(AxeItem.class)
public class AxeItemMixin {
    @Final
    @Shadow
    protected static Map<Block, Block> STRIPPABLES;

    @Inject(method = "getStripped", at=@At("HEAD"), cancellable = true)
    void getStripped(BlockState unstrippedState, CallbackInfoReturnable<Optional<BlockState>> cir) {
        if (unstrippedState.getBlock() == Blocks.BLACK_LOG.get()) {
            cir.setReturnValue(Optional.of(Blocks.STRIPPED_BLACK_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, unstrippedState.getValue(RotatedPillarBlock.AXIS))));
        }
    }
    @Inject(method = "getAxeStrippingState", at=@At("HEAD"), cancellable = true)
    private static void getAxeStrippingState(BlockState originalState, CallbackInfoReturnable<BlockState> cir) {
        if (originalState.getBlock() == Blocks.BLACK_LOG.get()) {
            cir.setReturnValue(Blocks.STRIPPED_BLACK_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, originalState.getValue(RotatedPillarBlock.AXIS)));
        }
    }
}