package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLLightDirection;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockModel.class)
public class BlockModelMixin {
    @ModifyReturnValue(method = "bakeFace", at = @At("RETURN"))
    private static BakedQuad sbl$bakeFace(BakedQuad original, @Local(name = "face") BlockElementFace face, @Local(name = "facing") Direction direction, @Local(name = "element") BlockElement element, @Local(name = "state") ModelState state) {
        if (face == null) return original;
        if (original instanceof SBLLightDirection sblQuad) {
            if ((Object) face instanceof SBLLightDirection sblFace) {
                var d = sblFace.sbl$getLightDirection();
                if (d == null) return original;
                sblQuad.sbl$setLightDirection(Direction.rotate(state.getRotation().getMatrix(), d));
            }
        }
        return original;
    }
}
