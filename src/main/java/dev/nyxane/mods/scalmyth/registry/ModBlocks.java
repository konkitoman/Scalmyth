package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenGrassBlock;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import dev.nyxane.mods.scalmyth.blocks.BloodFlowerBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlocks {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ScalmythAPI.MOD_ID);
  public static final DeferredBlock<Block> ASHEN_GRASS = BLOCKS.register("ashen_grass", AshenGrassBlock::new);
  public static final DeferredBlock<Block> ASHEN_SHORT_GRASS = BLOCKS.register(
          "ashen_short_grass",
          () -> new AshenShortGrassBlock(
                  BlockBehaviour.Properties.of()
                          .mapColor(MapColor.PLANT)
                          .replaceable()
                          .noCollission()
                          .instabreak()
                          .sound(SoundType.GRASS)
                          .offsetType(BlockBehaviour.OffsetType.XYZ)
                          .ignitedByLava()
                          .pushReaction(PushReaction.DESTROY)
                          .noOcclusion()
          ));

    public static final DeferredBlock<Block> BLOOD_FLOWER = BLOCKS.register(
            "bloodflower",
            () -> new BloodFlowerBlock(MobEffects.DARKNESS, 1,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .replaceable()
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .offsetType(BlockBehaviour.OffsetType.XYZ)
                            .ignitedByLava()
                            .pushReaction(PushReaction.DESTROY)
                            .noOcclusion()
            ));

    public static final DeferredBlock<Block> POTTED_BLOOD_FLOWER = BLOCKS.register("potted_bloodflower", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, BLOOD_FLOWER,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .instabreak()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY)
                    .noOcclusion()
            ));


  public static final DeferredBlock<Block> ASHEN_LOG = BLOCKS.register("ashen_log",
          () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .instrument(NoteBlockInstrument.BASS)
                    .sound(SoundType.WOOD)
                    // .ignitedByLava()
          ));
    public static final DeferredBlock<Block> ASHEN_WOOD = BLOCKS.register("ashen_wood",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                    // .ignitedByLava()
            ));
  public static final DeferredBlock<Block> STRIPPED_ASHEN_LOG = BLOCKS.register("stripped_ashen_log",
          () -> new RotatedPillarBlock(
                  BlockBehaviour.Properties.of()
                          .strength(2.0F)
                          .instrument(NoteBlockInstrument.BASS)
                          .sound(SoundType.WOOD)
                  // .ignitedByLava()
          ));

    public static final DeferredBlock<Block> STRIPPED_ASHEN_WOOD = BLOCKS.register("stripped_ashen_wood",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_PLANKS = BLOCKS.register("ashen_planks",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_SLAB = BLOCKS.register("ashen_slab",
            () -> new SlabBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_STAIR = BLOCKS.register("ashen_stair",
            () -> new StairBlock(ModBlocks.ASHEN_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_FENCE = BLOCKS.register("ashen_fence",
            () -> new FenceBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_PRESSURE_PLATE = BLOCKS.register("ashen_pressure_plate",
            () -> new PressurePlateBlock(new BlockSetType("ashen"),
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_BUTTON = BLOCKS.register("ashen_button",
            () -> new ButtonBlock(new BlockSetType("ashen"), 30,
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    // .ignitedByLava()
            ));

    public static final DeferredBlock<Block> ASHEN_FENCE_GATE = BLOCKS.register("ashen_fence_gate",
            () -> new FenceGateBlock(new WoodType("ashen", new BlockSetType("ashen")),
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .instrument(NoteBlockInstrument.BASS)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                    // .ignitedByLava()
            ));

  public static final DeferredBlock<Block> ASHEN_DOOR = BLOCKS.register("ashen_door",
          () -> new DoorBlock(
                  new BlockSetType("black"),
                  BlockBehaviour.Properties.of()
                          .strength(2.0f)
                          .sound(SoundType.WOOD)
                          .noOcclusion()
          ));
  public static final DeferredBlock<Block> ASHEN_LEAVES = BLOCKS.register("ashen_leaves",
          () -> new LeavesBlock(
                  BlockBehaviour.Properties.of()
                          .mapColor(MapColor.PLANT)
                          .strength(0.2F)
                          .sound(SoundType.CHERRY_LEAVES)
                          .randomTicks()
                          .noOcclusion()
                          .isValidSpawn(Blocks::ocelotOrParrot)
                          .ignitedByLava()
                          .pushReaction(PushReaction.DESTROY)
          ));

    public static final DeferredBlock<Block> ASHEN_TRAPDOOR = BLOCKS.register("ashen_trapdoor",
          () -> new TrapDoorBlock(
                  new BlockSetType("black"),
                  BlockBehaviour.Properties.of()
                          .strength(2.0f)
                          .sound(SoundType.WOOD)
          ));


  public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
  }
}
