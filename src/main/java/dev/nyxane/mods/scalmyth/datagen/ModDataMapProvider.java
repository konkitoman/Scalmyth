package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

public class ModDataMapProvider extends DataMapProvider {
    protected ModDataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        this.builder(NeoForgeDataMaps.FURNACE_FUELS)
                .add(ModItems.ASHEN_LOG.getId(), new FurnaceFuel(300), false)
                .add(ModItems.STRIPPED_ASHEN_LOG.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_PLANKS.getId(), new FurnaceFuel(300/4), false)
                .add(ModItems.ASHEN_WOOD.getId(), new FurnaceFuel(300), false)
                .add(ModItems.STRIPPED_ASHEN_WOOD.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_DOOR.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_SLAB.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_STAIR.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_FENCE.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_FENCE_GATE.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_PRESSURE_PLATE.getId(), new FurnaceFuel(300), false)
                .add(ModItems.ASHEN_BUTTON.getId(), new FurnaceFuel(100), false)
                .add(ModItems.ASHEN_TRAPDOOR.getId(), new FurnaceFuel(300), false);
    }
}
