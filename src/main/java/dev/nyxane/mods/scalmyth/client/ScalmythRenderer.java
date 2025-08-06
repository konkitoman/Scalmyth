package dev.nyxane.mods.scalmyth.client;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ScalmythRenderer extends GeoEntityRenderer<ScalmythEntity> {

    public ScalmythRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ScalmythModel());
    }
}
