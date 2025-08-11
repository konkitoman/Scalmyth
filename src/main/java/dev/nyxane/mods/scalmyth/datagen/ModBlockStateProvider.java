package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.Blocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ScalmythAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(Blocks.ASHEN_GRASS);
        blockWithItem(Blocks.BLACK_LOG);
        blockWithItem(Blocks.STRIPPED_BLACK_LOG);
        blockItem(Blocks.ASHEN_GRASS);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
    private void blockItem(DeferredBlock<?> deferredBlock) {
        // TODO: Unhardcode modid
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("scalmyth:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        // TODO: Unhardcode modid
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("scalmyth:block/" + deferredBlock.getId().getPath() + appendix));
    }
}
