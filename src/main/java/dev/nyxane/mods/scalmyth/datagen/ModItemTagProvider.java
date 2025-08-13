package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static dev.nyxane.mods.scalmyth.registry.ModItems.TAG_LOGS;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ScalmythAPI.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModItems.ASHEN_LOG.get())
                .add(ModItems.STRIPPED_ASHEN_LOG.get());
        tag(ItemTags.WOODEN_FENCES).add(ModItems.ASHEN_FENCE.get());
        tag(ItemTags.WOODEN_BUTTONS).add(ModItems.ASHEN_BUTTON.get());
        tag(ItemTags.WOODEN_DOORS).add(ModItems.ASHEN_DOOR.get());
        tag(ItemTags.WOODEN_PRESSURE_PLATES).add(ModItems.ASHEN_PRESSURE_PLATE.get());
        tag(ItemTags.WOODEN_SLABS).add(ModItems.ASHEN_SLAB.get());
        tag(ItemTags.WOODEN_STAIRS).add(ModItems.ASHEN_STAIR.get());
        tag(ItemTags.WOODEN_TRAPDOORS).add(ModItems.ASHEN_TRAPDOOR.get());
        tag(ItemTags.PLANKS).add(ModItems.ASHEN_PLANKS.get());
        tag(ItemTags.DIRT).add(ModItems.ASHEN_GRASS.get());
        tag(ItemTags.FLOWERS).add(ModItems.BLOOD_FLOWER.get());
        tag(TAG_LOGS)
                .add(ModItems.ASHEN_LOG.get())
                .add(ModItems.STRIPPED_ASHEN_LOG.get())
                .add(ModItems.ASHEN_WOOD.get())
                .add(ModItems.STRIPPED_ASHEN_WOOD.get());
    }
}
