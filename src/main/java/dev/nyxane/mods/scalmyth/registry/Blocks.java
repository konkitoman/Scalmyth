package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.AshenGrassBlock;
import dev.nyxane.mods.scalmyth.blocks.AshenShortGrassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.function.Supplier;

public class Blocks {
  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ScalmythAPI.MOD_ID);
  public static final DeferredBlock<Block> ASHEN_GRASS = BLOCKS.register("ashen_grass", AshenGrassBlock::new);
  public static final DeferredBlock<Block> ASHEN_SHORT_GRASS = BLOCKS.register("ashen_short_grass", AshenShortGrassBlock::new);
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
//  DeferredHolder<Block,? extends Block> PEDESTAL = registerBlock("pedestal", () -> new PedestalBlock());
    //redid the register to be more viable for bulk i guess, my methods are whatever though

//  net.minecraft.world.level.block.Blocks

  private static <T extends Block> DeferredHolder<Block,T> registerBlock(String name, Supplier<T> block) {
    DeferredHolder<Block, T> ret = BLOCKS.register(name,block);
//    registerBlockItem(name, block);
    return ret;
  }

  private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {

  }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
