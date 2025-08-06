package dev.nyxane.mods.scalmyth.client;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ScalmythModel extends GeoModel<ScalmythEntity> {
    @Override
    public ResourceLocation getModelResource(ScalmythEntity animatable) {
        return ScalmythAPI.rl("geo/scalamyth.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ScalmythEntity animatable) {
        return ScalmythAPI.rl("textures/scalamyth.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ScalmythEntity animatable) {
        return ScalmythAPI.rl("animations/scalamyth.animation.json");

    }
}
