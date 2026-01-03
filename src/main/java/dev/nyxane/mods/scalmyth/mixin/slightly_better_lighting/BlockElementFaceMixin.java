package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLLightDirection;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockElementFace.class)
public class BlockElementFaceMixin implements SBLLightDirection {
    @Unique
    Direction light_direction;

    @Override
    public Direction sbl$getLightDirection() {
        return light_direction;
    }

    @Override
    public void sbl$setLightDirection(Direction direction) {
        light_direction = direction;
    }
}
