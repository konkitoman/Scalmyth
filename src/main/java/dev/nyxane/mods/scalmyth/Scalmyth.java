package dev.nyxane.mods.scalmyth;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.client.ScalmythRenderer;
import dev.nyxane.mods.scalmyth.registry.*;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import dev.nyxane.mods.scalmyth.extrastuff.ObjImporter;
import dev.nyxane.mods.scalmyth.extrastuff.ObjImporterNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ScalmythAPI.MOD_ID)
public class Scalmyth {
    public Scalmyth(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.register(modEventBus);
        ModTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        modEventBus.addListener(ObjImporterNetworking::register);
        ModComponents.DATA_COMPONENTS.register(modEventBus);
    }

    @EventBusSubscriber(modid = ScalmythAPI.MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.SCALMYTH.get(), ScalmythRenderer::new);
        }

        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.BLOOD_FLOWER.getId(), ModBlocks.POTTED_BLOOD_FLOWER);
            });

        }

        @SubscribeEvent
        public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(KDebug.KDebugPayload.TYPE, KDebug.KDebugPayload.STREAM_CODEC, (a, b) -> {
                KDebug.addShape(b.player().level(), a.shape);
            });
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            KDebug.registerCommands(event.getDispatcher(), event.getBuildContext());
            ObjImporter.registerCommand(event);
        }

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            KDebug.registerClientCommands(event.getDispatcher(), event.getBuildContext());
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            KDebug.serverTick();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            KDebug.clean();
        }
    }

    @EventBusSubscriber(modid = ScalmythAPI.MOD_ID, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents {
        @SubscribeEvent
        public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playBidirectional(KDebug.KDebugPayload.TYPE, KDebug.KDebugPayload.STREAM_CODEC, (a, b) -> {
                KDebug.addShape(b.player().level(), a.shape);
            });
        }

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            KDebug.registerCommands(event.getDispatcher(), event.getBuildContext());
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            KDebug.serverTick();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            KDebug.clean();
        }
    }
}
