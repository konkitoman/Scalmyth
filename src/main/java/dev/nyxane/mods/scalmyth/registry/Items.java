package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import dev.nyxane.mods.scalmyth.registry.Blocks;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

public class Items {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(ScalmythAPI.MOD_ID);
    public static final DeferredItem<Item> SCALMYTH_SPAWN_EGG = REGISTRY.register("scalmyth_spawn_egg", () -> new DeferredSpawnEggItem(Entities.SCALMYTH, -1, -1, new Item.Properties()));
    public static final DeferredItem<Item> ASHEN_GRASS = block(Blocks.ASHEN_GRASS);

    // Start of user code block custom items
    // End of user code block custom items
    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}