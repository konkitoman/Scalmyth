package dev.nyxane.mods.scalmyth.client;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.entity.ScalmythEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ScalmythRenderer extends GeoEntityRenderer<ScalmythEntity> {
    public ScalmythRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(ScalmythAPI.rl("scalmyth"), true));

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
