package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.Blocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Objects;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ScalmythAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        logBlock((RotatedPillarBlock) Blocks.BLACK_LOG.get());
        logBlock((RotatedPillarBlock) Blocks.STRIPPED_BLACK_LOG.get());
        blockWithItem(Blocks.ASHEN_SHORT_GRASS);
        blockWithItem(Blocks.ASHEN_GRASS);

    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        blockWithItem(deferredBlock, "");
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockWithItem(deferredBlock.get(), blockModelFile(blockName(deferredBlock) + appendix));
    }

    private ModelFile blockModelFile(String path) {
        return new ModelFile.UncheckedModelFile(ScalmythAPI.MOD_ID + ":" + "block/" + path);
    }

    private String blockName(DeferredBlock<?> block) {
        return Objects.requireNonNull(block.getId().getPath());
    }

    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath() + suffix);
    }
}
