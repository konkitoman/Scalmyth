package dev.nyxane.mods.scalmyth.mixin;

import com.mojang.math.Transformation;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.DynamicBlock;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Mixin(value = BlockModelShaper.class)
public class BlockModelShaperMixin {
    @Inject(method = "getBlockModel", at = @At("HEAD"), cancellable = true)
    void getBlockModel(BlockState state, CallbackInfoReturnable<BakedModel> cir){
        if (state.is(ModBlocks.DYNAMIC_BLOCK)){
            var id = state.getValue(DynamicBlock.ID);
            ScalmythAPI.LOGGER.info("getBlockModel: {}", id);

            var mc = Minecraft.getInstance();
            var atlas = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            var atlasSprite = atlas.getSprite(ScalmythAPI.rl("block/wall"));
            var model = new SimpleBakedModel(List.of(), makeEmptyCulledFaces(atlasSprite, id), false, true, false, UnitTextureAtlasSprite.INSTANCE, ItemTransforms.NO_TRANSFORMS, ItemOverrides.EMPTY, new RenderTypeGroup(RenderType.SOLID, RenderType.ENTITY_GLINT, RenderType.ENTITY_GLINT));
            cir.setReturnValue(model);
        }
    }

    private static Map<Direction, List<BakedQuad>> makeEmptyCulledFaces(TextureAtlasSprite sprite, int id) {
        Map<Direction, List<BakedQuad>> map = new EnumMap(Direction.class);

        var faceB = new FaceBakery();
        var state = new SimpleModelState(Transformation.identity());

        for(Direction direction : Direction.values()) {
            float[] UV = {0, 0, 16, 8};
            var face = faceB.bakeQuad(
                new Vector3f(0, 0 + id, 0),
                new Vector3f(16, 8 + id, 2),
                new BlockElementFace(direction, 0, "block/wall", new BlockFaceUV(UV, 0)),
                sprite,
                direction,
                state,
                new BlockElementRotation(new Vector3f(0, 0, 0), Direction.Axis.X, 0, false),
                false);
            map.put(direction, List.of(face));
        }

        return map;
    }
}
