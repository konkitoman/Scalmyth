package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;

import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import dev.nyxane.mods.scalmyth.items.AshDustItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
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
    public static final DeferredItem<Item> ASH_DUST = REGISTRY.register("ash_dust", AshDustItem::new);
    //public static final DeferredItem<Item> ASHEN_SHORT_GRASS = block(Blocks.ASHEN_SHORT_GRASS);
    public static final DeferredItem<Item> ASHEN_SHORT_GRASS = REGISTRY.register(
            "ashen_short_grass",
            () -> new BlockItem(Blocks.ASHEN_SHORT_GRASS.get(), new Item.Properties()) {
                @Override
                protected BlockState getPlacementState(BlockPlaceContext context) {
                    BlockState state = super.getPlacementState(context);
                    if (state != null) {
                        state = state.setValue(AshenShortGrassBlock.VARIANT, AshenShortGrassBlock.Variant.SHORT);
                    }
                    return state;
                }
            }
    );
    public static final DeferredItem<Item> ASHEN_TALL_GRASS = REGISTRY.register(
            "ashen_tall_grass",
            () -> new BlockItem(Blocks.ASHEN_SHORT_GRASS.get(), new Item.Properties()) {
                @Override
                protected BlockState getPlacementState(BlockPlaceContext context) {
                    BlockState state = super.getPlacementState(context);
                    if (state != null) {
                        state = state.setValue(AshenShortGrassBlock.VARIANT, AshenShortGrassBlock.Variant.TALL);
                    }
                    return state;
                }
            }
    );
    public static final DeferredItem<Item> BLACK_LOG = block(Blocks.BLACK_LOG);
    public static final DeferredItem<Item> STRIPPED_BLACK_LOG = block(Blocks.STRIPPED_BLACK_LOG);

    // Start of user code block custom items
    // End of user code block custom items
    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
        return block(block, new Item.Properties());
    }

    private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
        return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
    }
}