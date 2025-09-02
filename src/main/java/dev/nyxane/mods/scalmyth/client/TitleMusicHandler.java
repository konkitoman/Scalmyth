package dev.nyxane.mods.scalmyth.client;

import dev.nyxane.mods.scalmyth.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class TitleMusicHandler {

    private static boolean isPlaying = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof TitleScreen) {
            if (!isPlaying) {
                // Play your custom music as a looping SoundInstance
                mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.TITLE_MUSIC, 1.0F));
                isPlaying = true;
            }
        } else {
            isPlaying = false; // reset flag when leaving title screen
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() != null && "music.menu".equals(event.getSound().getLocation().getPath())) {
            event.setSound(null); // cancel default title music
        }
    }
}
