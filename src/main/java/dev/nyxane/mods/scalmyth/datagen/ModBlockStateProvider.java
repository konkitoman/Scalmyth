package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.fml.common.Mod;
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
        simpleBlock(ModBlocks.ASHEN_PLANKS.get());
        blockItem(ModBlocks.ASHEN_PLANKS);
        slabBlock((SlabBlock)ModBlocks.ASHEN_SLAB.get(),blockTexture(ModBlocks.ASHEN_PLANKS.get()),blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        blockItem(ModBlocks.ASHEN_SLAB);
        stairsBlock((StairBlock)ModBlocks.ASHEN_STAIR.get(),blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        blockItem(ModBlocks.ASHEN_STAIR);
        fenceBlock((FenceBlock)ModBlocks.ASHEN_FENCE.get(),blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        fenceGateBlock((FenceGateBlock) ModBlocks.ASHEN_FENCE_GATE.get(),blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        blockItem(ModBlocks.ASHEN_FENCE_GATE);
        pressurePlateBlock((PressurePlateBlock)ModBlocks.ASHEN_PRESSURE_PLATE.get(),blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        blockItem(ModBlocks.ASHEN_PRESSURE_PLATE);
        buttonBlock((ButtonBlock)ModBlocks.ASHEN_BUTTON.get(), blockTexture(ModBlocks.ASHEN_PLANKS.get()));
        logBlock((RotatedPillarBlock) ModBlocks.ASHEN_LOG.get());
        blockItem(ModBlocks.ASHEN_LOG);


        axisBlock((RotatedPillarBlock) ModBlocks.ASHEN_WOOD.get(), ScalmythAPI.rl("block/ashen_log"), ScalmythAPI.rl("block/ashen_log"));
        blockItem(ModBlocks.ASHEN_WOOD);

        axisBlock((RotatedPillarBlock) ModBlocks.STRIPPED_ASHEN_WOOD.get(), ScalmythAPI.rl("block/stripped_ashen_log"), ScalmythAPI.rl("block/stripped_ashen_log"));
        blockItem(ModBlocks.STRIPPED_ASHEN_WOOD);


        logBlock((RotatedPillarBlock) ModBlocks.STRIPPED_ASHEN_LOG.get());
        blockItem(ModBlocks.STRIPPED_ASHEN_LOG);

        block(ModBlocks.ASHEN_GRASS);
        blockItem(ModBlocks.ASHEN_GRASS);

        doorBlockWithRenderType((DoorBlock) ModBlocks.ASHEN_DOOR.get(),
                blockTexture(ModBlocks.ASHEN_DOOR.get(), "_bottom"),
                blockTexture(ModBlocks.ASHEN_DOOR.get(), "_top"),
                "cutout"
        );

        block(ModBlocks.ASHEN_LEAVES);
        blockItem(ModBlocks.ASHEN_LEAVES);

        getVariantBuilder(ModBlocks.ASHEN_SHORT_GRASS.get())
                .partialState().with(AshenShortGrassBlock.TALL, false)
                .modelForState().modelFile(blockModelFile(name(ModBlocks.ASHEN_SHORT_GRASS))).addModel()
                .partialState().with(AshenShortGrassBlock.TALL, true)
                .modelForState().modelFile(blockModelFile(name(ModBlocks.ASHEN_SHORT_GRASS)+"_long")).addModel();
        blockItem(ModBlocks.ASHEN_SHORT_GRASS);
        trapdoorBlock((TrapDoorBlock) ModBlocks.ASHEN_TRAPDOOR.get(), blockTexture(ModBlocks.ASHEN_TRAPDOOR.get()), false);
        simpleBlockItem(ModBlocks.ASHEN_TRAPDOOR.get(), blockModelFile(name(ModBlocks.ASHEN_TRAPDOOR) + "_bottom"));
    }

    private ResourceLocation blockTexture(Block block, String suffix) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + name.getPath()+suffix);
    }

    private void block(DeferredBlock<?> deferredBlock) {
        block(deferredBlock, "");
    }
    private void block(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlock(deferredBlock.get(), blockModelFile(name(deferredBlock) + appendix));
    }
    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), blockModelFile(name(deferredBlock)));
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
