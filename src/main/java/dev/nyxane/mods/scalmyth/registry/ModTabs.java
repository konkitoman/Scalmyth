package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

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
        output.accept(ModItems.BLOOD_FLOWER.get().asItem());
        output.accept(ModItems.ASHEN_SHORT_GRASS.get().asItem());
        output.accept(ModItems.ASH_DUST.get());
        output.accept(ModItems.ASHEN_LOG.get());
        output.accept(ModItems.ASHEN_WOOD.get());
        output.accept(ModItems.STRIPPED_ASHEN_LOG.get());
        output.accept(ModItems.STRIPPED_ASHEN_WOOD.get());
        output.accept(ModItems.ASHEN_BRANCH.get());
        output.accept(ModItems.ASHEN_DOOR.get());
        output.accept(ModItems.ASHEN_LEAVES.get());
        output.accept(ModItems.ASHEN_PLANKS.get());
        output.accept(ModItems.ASHEN_SLAB.get());
        output.accept(ModItems.ASHEN_STAIR.get());
        output.accept(ModItems.ASHEN_FENCE.get());
        output.accept(ModItems.ASHEN_FENCE_GATE.get());
        output.accept(ModItems.ASHEN_PRESSURE_PLATE.get());
        output.accept(ModItems.ASHEN_BUTTON.get());
        output.accept(ModItems.ASHEN_TRAPDOOR.get());
        output.accept(ModItems.ASHEN_STONE_BRICK.get());
        output.accept(ModItems.ASHEN_GROOVED_STONE_BRICK.get());
        output.accept(ModItems.ASHEN_TALL_GRASS.get());
        output.accept(ModItems.ASHEN_BRICKS.get());
        output.accept(ModItems.ASHEN_VINES.get());
        output.accept(ModItems.LARGE_ASHEN_FERN.get());
        output.accept(ModItems.ASHEN_FERN.get());
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}