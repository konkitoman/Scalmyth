package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.blocks.PedestalBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Blocks {
  public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ScalmythAPI.MOD_ID);

//  DeferredHolder<Block,? extends Block> PEDESTAL = registerBlock("pedestal", () -> new PedestalBlock());

  private static <T extends Block> DeferredHolder<Block,T> registerBlock(String name, Supplier<T> block) {
    DeferredHolder<Block, T> ret = BLOCKS.register(name,block);
//    registerBlockItem(name, block);
    return ret;
  }

  private static <T extends Block> void registerBlockItem(String name, DeferredHolder<Block, T> block) {

  }
}
