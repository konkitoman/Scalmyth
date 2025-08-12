package dev.nyxane.mods.scalmyth;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.client.ScalmythRenderer;
import dev.nyxane.mods.scalmyth.registry.ModEntities;
import dev.nyxane.mods.scalmyth.registry.ModSounds;
import dev.nyxane.mods.scalmyth.registry.ModItems;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import dev.nyxane.mods.scalmyth.registry.ModTabs;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(ScalmythAPI.MOD_ID)
public class Scalmyth
{
  public Scalmyth(IEventBus modEventBus, ModContainer modContainer)
  {
    ModEntities.register(modEventBus);
    ModTabs.REGISTRY.register(modEventBus);
    ModSounds.register(modEventBus);
    ModItems.REGISTRY.register(modEventBus);
    ModBlocks.register(modEventBus);
  }
  @EventBusSubscriber(modid = ScalmythAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      EntityRenderers.register(ModEntities.SCALMYTH.get(), ScalmythRenderer::new);
    }
  }
}
