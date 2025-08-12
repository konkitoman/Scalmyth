package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenGrassBlock;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlocks {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ScalmythAPI.MOD_ID);
  public static final DeferredBlock<Block> ASHEN_GRASS = BLOCKS.register("ashen_grass", AshenGrassBlock::new);
  public static final DeferredBlock<AshenShortGrassBlock> ASHEN_SHORT_GRASS = BLOCKS.register(
          "ashen_short_grass",
          AshenShortGrassBlock::new
  );

  public static final DeferredBlock<Block> BLACK_LOG = BLOCKS.register("black_log",
          () -> new RotatedPillarBlock(
            BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .instrument(NoteBlockInstrument.BASS)
                    .sound(SoundType.WOOD)
                    // .ignitedByLava()
          ));
  public static final DeferredBlock<Block> STRIPPED_BLACK_LOG = BLOCKS.register("stripped_black_log",
          () -> new RotatedPillarBlock(
                  BlockBehaviour.Properties.of()
                          .strength(2.0F)
                          .instrument(NoteBlockInstrument.BASS)
                          .sound(SoundType.WOOD)
                  // .ignitedByLava()
          ));

  public static final DeferredBlock<Block> BLACK_DOOR = BLOCKS.register("black_door",
          () -> new DoorBlock(
                  new BlockSetType("black"),
                  BlockBehaviour.Properties.of()
                          .strength(2.0f)
                          .sound(SoundType.WOOD)
          ));
  public static final DeferredBlock<Block> BLACK_LEAVES = BLOCKS.register("black_leaves",
          () -> new LeavesBlock(
                  BlockBehaviour.Properties.of()
                          .mapColor(MapColor.PLANT)
                          .strength(0.2F)
                          .randomTicks()
                          .noOcclusion()
                          .isValidSpawn(Blocks::ocelotOrParrot)
                          .ignitedByLava()
                          .pushReaction(PushReaction.DESTROY)
          ));

  public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
  }
}
