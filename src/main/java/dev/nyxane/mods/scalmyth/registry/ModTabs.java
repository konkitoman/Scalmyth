package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScalmythAPI.MOD_ID);

    public static final Supplier<CreativeModeTab> SCALMYTH_TAB =
            REGISTRY.register("scalmyth_tab",
                () -> CreativeModeTab.builder()
                        .icon(() -> new ItemStack(ModItems.ASHEN_GRASS.get()))
                        .title(Component.translatable("itemGroup.scalmyth"))
                        .displayItems(ModTabs::acceptItems)
                        .build());

    private static void acceptItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                                    CreativeModeTab.Output output) {
        output.accept(ModItems.SCALMYTH_SPAWN_EGG.get().asItem());
        output.accept(ModItems.ASHEN_GRASS.get().asItem());
        output.accept(ModItems.ASHEN_SHORT_GRASS.get().asItem());
        output.accept(ModItems.ASH_DUST.get());
        output.accept(ModItems.BLACK_LOG.get());
        output.accept(ModItems.STRIPPED_BLACK_LOG.get());
        output.accept(ModItems.BLACK_DOOR.get());
        output.accept(ModItems.BLACK_LEAVES.get());
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}