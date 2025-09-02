package dev.nyxane.mods.scalmyth.mixin;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockElement.Deserializer.class)
public class BlockElement$DeserializerMixin {
    @Inject(at = @At("HEAD"), method = "getAngle", cancellable = true)
    private void getAngle(JsonObject json, CallbackInfoReturnable<Float> cir){
        cir.setReturnValue(GsonHelper.getAsFloat(json, "angle"));
    }
}
