package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        otherWhenSilkTouch(ModBlocks.ASHEN_GRASS.get(), net.minecraft.world.level.block.Blocks.DIRT);
        dropWhenSilkTouch(ModBlocks.ASHEN_SHORT_GRASS.get());
        dropSelf(ModBlocks.BLACK_LOG.get());
        dropSelf(ModBlocks.STRIPPED_BLACK_LOG.get());
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
