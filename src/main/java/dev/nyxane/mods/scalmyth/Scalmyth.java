package dev.nyxane.mods.scalmyth;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.client.ScalmythRenderer;
import dev.nyxane.mods.scalmyth.registry.Entities;
import dev.nyxane.mods.scalmyth.registry.Sounds;
import dev.nyxane.mods.scalmyth.registry.Items;
import dev.nyxane.mods.scalmyth.registry.Blocks;
import dev.nyxane.mods.scalmyth.registry.Tabs;
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
    Entities.register(modEventBus);
    Tabs.REGISTRY.register(modEventBus);
    Sounds.register(modEventBus);
    Items.REGISTRY.register(modEventBus);
    Blocks.register(modEventBus);
  }
  @EventBusSubscriber(modid = ScalmythAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

      EntityRenderers.register(Entities.SCALMYTH.get(), ScalmythRenderer::new);
    }
  }
}
