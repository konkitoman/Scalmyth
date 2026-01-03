package dev.nyxane.mods.scalmyth.mixin.slightly_better_lighting;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import dev.nyxane.mods.scalmyth.slightly_better_lighting.SBLLightDirection;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockElementFace.Deserializer.class)
public class BlockElementFace$DeserializerMixin {
    @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockElementFace;", at = @At("RETURN"))
    private BlockElementFace sbl$deserialize(BlockElementFace original, @Local(name = "json") JsonElement json) {
        if (json == null || !json.isJsonObject()) return original;
        var light = json.getAsJsonObject().get("sbl$light");
        if (light == null || !light.isJsonPrimitive()) return original;

        var direction = Direction.CODEC.parse(JsonOps.INSTANCE, light);
        if (direction.isError()) return original;

        if ((Object) original instanceof SBLLightDirection sblFace) {
            sblFace.sbl$setLightDirection(direction.result().get());
        }
        return original;
    }
}
