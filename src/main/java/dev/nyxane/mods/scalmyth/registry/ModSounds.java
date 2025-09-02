package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
  public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
    DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ScalmythAPI.MOD_ID);

    public static final Supplier<SoundEvent> SCALMYTH_ATTACK = registerSoundEvent("scalmyth.attack");
  public static final Supplier<SoundEvent> SCALMYTH_AMBIENCE = registerSoundEvent("scalmyth.scalmyth_ambience");
  public static final Supplier<SoundEvent> SCALMYTH_ROAR = registerSoundEvent("scalmyth.scalmyth_roar");
    public static final Supplier<SoundEvent> SCALMYTH_DEATH = registerSoundEvent("scalmyth.death");
    public static final Supplier<SoundEvent> SCALMYTH_FOOTSTEPS = registerSoundEvent("scalmyth.scalmyth_step");
    public static final Supplier<SoundEvent> SCALMYTH_JUMP = registerSoundEvent("scalmyth.jump");
    public static final Supplier<SoundEvent> SCALMYTH_LAND = registerSoundEvent("scalmyth.land");
    public static final Supplier<SoundEvent> SCALMYTH_SHRIEK = registerSoundEvent("scalmyth.shriek");
    public static final Supplier<SoundEvent> SCALMYTH_IDLE = registerSoundEvent("scalmyth.idle");
    public static final Supplier<SoundEvent> SCALMYTH_ASHEN_EARTH_PLACE = registerSoundEvent("ashen.earth.place");
    public static final Supplier<SoundEvent> SCALMYTH_ASHEN_EARTH_BREAK = registerSoundEvent("ashen.earth.break");
    public static final Supplier<SoundEvent> SCALMYTH_ASHEN_EARTH_MINE = registerSoundEvent("ashen.earth.mine");
    public static final Supplier<SoundEvent> SCALMYTH_ASHEN_EARTH_STEP = registerSoundEvent("ashen.earth.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> TITLE_MUSIC = registerSoundHolderEvent("titlescreen");

  private static Supplier<SoundEvent> registerSoundEvent(String name) {
    ResourceLocation id = ScalmythAPI.rl(name);
    return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
  }
  private static DeferredHolder<SoundEvent, SoundEvent> registerSoundHolderEvent(String name) {
    ResourceLocation id = ScalmythAPI.rl(name);
    return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
  }

  public static void register(IEventBus eventBus) {
    SOUND_EVENTS.register(eventBus);
  }
}
