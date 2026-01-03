package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLLightDirection;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;

@Pseudo
@Mixin(FlatLightPipeline.class)
public abstract class Sodium$FlatLightPipelineMixin {
    @Shadow
    abstract int getOffsetLightmap(BlockPos pos, Direction face);

    @Shadow
    @Final
    private LightDataAccess lightCache;

    @Inject(method = "calculate", at = @At(value = "INVOKE", target = "Ljava/util/Arrays;fill([II)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private void sbl$calculate(ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace, Direction lightFace, boolean shade, boolean enhanced, CallbackInfo ci, @Local(name = "lightmap") LocalIntRef lightmap) {
        if (quad instanceof SBLLightDirection sblQuad) {
            var d = sblQuad.sbl$getLightDirection();
            if (d == null) return;
            lightmap.set(this.getOffsetLightmap(pos, d));
            Arrays.fill(out.br, lightCache.getLevel().getShade(d, shade));
        }
    }
}
