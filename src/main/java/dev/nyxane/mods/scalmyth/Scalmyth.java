package dev.nyxane.mods.scalmyth;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.client.ScalmythRenderer;
import dev.nyxane.mods.scalmyth.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ScalmythAPI.MOD_ID)
public class Scalmyth
{
    public Scalmyth(IEventBus modEventBus, ModContainer modContainer)  {
        ModEntities.register(modEventBus);
        ModTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
    }


    @EventBusSubscriber(modid = ScalmythAPI.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.SCALMYTH.get(), ScalmythRenderer::new);


        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event){
            event.enqueueWork(() -> {
                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.BLOOD_FLOWER.getId(), ModBlocks.POTTED_BLOOD_FLOWER);
            });
        }

        @SubscribeEvent
        public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event){
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(KDebug.KDebugPayload.TYPE, KDebug.KDebugPayload.STREAM_CODEC, (a, b) -> {
                KDebug.addShape(b.player().level(), a.shape);
            });
        }
    }
}
