package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import dev.nyxane.mods.scalmyth.registry.Items;
import dev.nyxane.mods.scalmyth.registry.Blocks;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Tabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScalmythAPI.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SCALMYTH_TAB = REGISTRY.register("scalmyth_tab",
            () -> CreativeModeTab.builder().title(Component.translatable("item_group.scalmyth.scalmyth_tab")).icon(() -> new ItemStack(Blocks.ASHEN_GRASS.get())).displayItems(
                    (parameters, tabData) -> {
                tabData.accept(Blocks.ASHEN_GRASS.get().asItem());
                }).build() //defined grass here for the sake of getting this working
    ); //my indentation is insane, WHATEVER, it works. i hate how dense this is though, but nothing is better? idfk, SHUT UP, gah! i hate this

    @SubscribeEvent
    public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
        if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            tabData.accept(Items.SCALMYTH_SPAWN_EGG.get());
        } else if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            tabData.accept(Blocks.ASHEN_GRASS.get().asItem());
        } else if (tabData.getTabKey().equals(SCALMYTH_TAB.getKey())) {
            tabData.accept(Items.SCALMYTH_SPAWN_EGG.get().asItem());
            tabData.accept(Items.ASHEN_SHORT_GRASS.get().asItem());
            tabData.accept(Items.ASH_DUST.get());
            tabData.accept(Items.BLACK_LOG.get());
            tabData.accept(Items.STRIPPED_BLACK_LOG.get());
        }
    }
}