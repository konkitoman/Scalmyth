package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.registry.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        otherWhenSilkTouch(Blocks.ASHEN_GRASS.get(), net.minecraft.world.level.block.Blocks.DIRT);
        dropWhenSilkTouch(Blocks.ASHEN_SHORT_GRASS.get());
        dropSelf(Blocks.BLACK_LOG.get());
        dropSelf(Blocks.STRIPPED_BLACK_LOG.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return Blocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
