package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.Objects;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ScalmythAPI.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        logBlock((RotatedPillarBlock) ModBlocks.BLACK_LOG.get());
        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_BLACK_LOG.get());
        blockWithItem(ModBlocks.ASHEN_GRASS);
        doorBlockWithRenderType((DoorBlock) ModBlocks.BLACK_DOOR.get(),
                blockTexture(ModBlocks.BLACK_DOOR.get(), "_bottom"),
                blockTexture(ModBlocks.BLACK_DOOR.get(), "_top"),
                "cutout"
        );
        blockWithItem(ModBlocks.BLACK_LEAVES);
        getVariantBuilder(ModBlocks.ASHEN_SHORT_GRASS.get())
                .partialState().with(AshenShortGrassBlock.TALL, false)
                .modelForState().modelFile(blockModelFile(name(ModBlocks.ASHEN_SHORT_GRASS))).addModel()
                .partialState().with(AshenShortGrassBlock.TALL, true)
                .modelForState().modelFile(blockModelFile(name(ModBlocks.ASHEN_SHORT_GRASS)+"_long")).addModel();
    }

    private ResourceLocation blockTexture(Block block, String suffix) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + name.getPath()+suffix);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        blockWithItem(deferredBlock, "");
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockWithItem(deferredBlock.get(), blockModelFile(name(deferredBlock) + appendix));
    }

    private ModelFile blockModelFile(String path) {
        return new ModelFile.UncheckedModelFile(ScalmythAPI.MOD_ID + ":" + "block/" + path);
    }

    private String name(DeferredBlock<?> block) {
        return Objects.requireNonNull(block.getId().getPath());
    }

    private ResourceLocation extend(ResourceLocation rl, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), rl.getPath() + suffix);
    }
}
