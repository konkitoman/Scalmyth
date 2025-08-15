package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.items.BloodCompassItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
            .createDataComponents(Registries.DATA_COMPONENT_TYPE, ScalmythAPI.MOD_ID);

    public static final Supplier<DataComponentType<BloodCompassItem.State>> BLOOD_COMPASS_STATE = DATA_COMPONENTS.registerComponentType(
            "blood_compass_state",
            builder -> builder
                    .networkSynchronized(new BloodCompassItem.StateCodec())
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
