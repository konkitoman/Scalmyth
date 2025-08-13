package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import dev.nyxane.mods.scalmyth.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ScalmythAPI.MOD_ID, existingFileHelper);
    }
    public void fenceItem(DeferredBlock<?> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",  ResourceLocation.fromNamespaceAndPath(ScalmythAPI.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.ASH_DUST.get());
        basicItem(ModItems.ASHEN_DOOR.get());
        fenceItem(ModBlocks.ASHEN_FENCE, ModBlocks.ASHEN_PLANKS);
        spawnEggItem(ModItems.SCALMYTH_SPAWN_EGG.get());
    }
}
