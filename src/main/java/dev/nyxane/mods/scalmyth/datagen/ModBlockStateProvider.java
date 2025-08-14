package dev.nyxane.mods.scalmyth.datagen;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
        block(ModBlocks.BLOOD_FLOWER);
        simpleBlockWithItem(ModBlocks.POTTED_BLOOD_FLOWER.get(), models().singleTexture("potted_bloodflower", ResourceLocation.withDefaultNamespace("flower_pot_cross"), "plant", blockTexture(ModBlocks.BLOOD_FLOWER.get())).renderType("cutout") );
        blockItem(ModBlocks.BLOOD_FLOWER);
        simpleBlock(ModBlocks.ASHEN_STONE_BRICK.get());
        blockItem(ModBlocks.ASHEN_STONE_BRICK);
        simpleBlock(ModBlocks.ASHEN_GROOVED_STONE_BRICK.get());
        blockItem(ModBlocks.ASHEN_GROOVED_STONE_BRICK);
        simpleBlock(ModBlocks.ASHEN_BRICKS.get());
        blockItem(ModBlocks.ASHEN_BRICKS);


        doorBlockWithRenderType((DoorBlock) ModBlocks.ASHEN_DOOR.get(),
                blockTexture(ModBlocks.ASHEN_DOOR.get(), "_bottom"),
                blockTexture(ModBlocks.ASHEN_DOOR.get(), "_top"),
                "cutout"
        );

        blockItem(ModBlocks.ASHEN_LEAVES);

        getVariantBuilder(ModBlocks.ASHEN_SHORT_GRASS.get())
                .partialState().with(AshenShortGrassBlock.TALL, false)
                .modelForState().modelFile(crossBlock(ModBlocks.ASHEN_SHORT_GRASS, "")).addModel()
                .partialState().with(AshenShortGrassBlock.TALL, true)
                .modelForState().modelFile(crossBlock(ModBlocks.ASHEN_SHORT_GRASS,"_long")).addModel();
        doublePlantBlock(ModBlocks.ASHEN_TALL_GRASS);
        doublePlantBlock(ModBlocks.LARGE_ASHEN_FERN);
        simpleBlock(ModBlocks.ASHEN_FERN.get(), crossBlock(ModBlocks.ASHEN_FERN, ""));

        trapdoorBlock((TrapDoorBlock) ModBlocks.ASHEN_TRAPDOOR.get(), blockTexture(ModBlocks.ASHEN_TRAPDOOR.get()), false);
        simpleBlockItem(ModBlocks.ASHEN_TRAPDOOR.get(), blockModelFile(name(ModBlocks.ASHEN_TRAPDOOR) + "_bottom"));
        blockItem(ModBlocks.ASHEN_VINES);
    }

    private ResourceLocation blockTexture(Block block, String suffix) {
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + name.getPath()+suffix);
    }

    private void doublePlantBlock(DeferredBlock<?> block) {
        getVariantBuilder(block.get())
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER)
                .modelForState().modelFile(crossBlock(block, "_top")).addModel()
                .partialState().with(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER)
                .modelForState().modelFile(crossBlock(block,"_bottom")).addModel();
    }

    private ModelFile crossBlock(DeferredBlock<?> block, String suffix) {
        return models().withExistingParent(block.getId().getPath()+suffix, "block/cross")
                .texture("cross", blockTexture(block.get(), suffix))
                .texture("particle", blockTexture(block.get(), suffix))
                .renderType("cutout");
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
