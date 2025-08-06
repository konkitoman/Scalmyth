package dev.nyxane.mods.scalmyth.client;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class ScalmythRenderer extends GeoEntityRenderer<ScalmythEntity> {

  public ScalmythRenderer(EntityRendererProvider.Context renderManager) {
    super(renderManager, new ScalmythModel());

    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
