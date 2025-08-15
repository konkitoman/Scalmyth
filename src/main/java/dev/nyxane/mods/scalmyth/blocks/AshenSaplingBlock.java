package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import dev.nyxane.mods.scalmyth.worldgen.tree.ModTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AshenSaplingBlock extends SaplingBlock {
    public AshenSaplingBlock() {
        this(ModTreeGrowers.ASHEN_TREE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));
    }

    public AshenSaplingBlock(TreeGrower treeGrower, Properties properties) {
        super(treeGrower, properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.ASHEN_GRASS.get());
    }
}
