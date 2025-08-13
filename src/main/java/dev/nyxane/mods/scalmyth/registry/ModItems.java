package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.items.AshDustItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

public class ModItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(ScalmythAPI.MOD_ID);
    public static final DeferredItem<Item> SCALMYTH_SPAWN_EGG = REGISTRY.register("scalmyth_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.SCALMYTH, -1, -1, new Item.Properties()));
    public static final DeferredItem<Item> ASHEN_GRASS = block(ModBlocks.ASHEN_GRASS);
    public static final DeferredItem<Item> ASH_DUST = REGISTRY.register("ash_dust", AshDustItem::new);
    public static final DeferredItem<Item> ASHEN_SHORT_GRASS = block(ModBlocks.ASHEN_SHORT_GRASS);
    public static final DeferredItem<Item> ASHEN_LOG = block(ModBlocks.ASHEN_LOG);
    public static final DeferredItem<Item> STRIPPED_ASHEN_LOG = block(ModBlocks.STRIPPED_ASHEN_LOG);
    public static final DeferredItem<Item> ASHEN_DOOR = block(ModBlocks.ASHEN_DOOR);
    public static final DeferredItem<Item> ASHEN_LEAVES = block(ModBlocks.ASHEN_LEAVES);
    public static final DeferredItem<Item> ASHEN_PLANKS = block(ModBlocks.ASHEN_PLANKS);
    public static final DeferredItem<Item> ASHEN_SLAB = block(ModBlocks.ASHEN_SLAB);
    public static final DeferredItem<Item> ASHEN_STAIR = block(ModBlocks.ASHEN_STAIR);
    public static final DeferredItem<Item> ASHEN_FENCE = block(ModBlocks.ASHEN_FENCE);
    public static final DeferredItem<Item> ASHEN_FENCE_GATE = block(ModBlocks.ASHEN_FENCE_GATE);
    public static final DeferredItem<Item> ASHEN_PRESSURE_PLATE = block(ModBlocks.ASHEN_PRESSURE_PLATE);


    private static DeferredItem<Item> block(DeferredBlock<?> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredBlock<?> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}