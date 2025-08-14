package dev.nyxane.mods.scalmyth;


import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static dev.nyxane.mods.scalmyth.api.ScalmythAPI.MOD_ID;

public class ScalmythClient {
    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {

            }
        }
    }


