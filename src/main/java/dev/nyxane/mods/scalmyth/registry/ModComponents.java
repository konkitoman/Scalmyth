package dev.nyxane.mods.scalmyth.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;


public final class ModComponents {
    public static final String MODID = "scalmyth";

    // Create the specialized DataComponents deferred register (uses Registries.DATA_COMPONENT_TYPE)
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    // Suppliers (DeferredHolder implements Supplier) — call get() at runtime to fetch the DataComponentType
    public static final Supplier<DataComponentType<Integer>> BATTERY_ENERGY =
            DATA_COMPONENTS.registerComponentType("battery_energy", builder ->
                    builder
                            .persistent(Codec.INT)                  // persistent to disk
                            .networkSynchronized(ByteBufCodecs.INT) // network codec for syncing
            );

    public static final Supplier<DataComponentType<Boolean>> FLASHLIGHT_ON =
            DATA_COMPONENTS.registerComponentType("flashlight_on", builder ->
                    builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    // Call this from your mod constructor / setup to register the data components on the mod bus:
    // ModComponents.register(eventBus);
    public static void register() {

    }
}
