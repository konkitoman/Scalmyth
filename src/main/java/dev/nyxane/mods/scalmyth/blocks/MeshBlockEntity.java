package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MeshBlockEntity extends BlockEntity {
    public MeshBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.MESH_ENTITY.get(), pos, blockState);
    }
}
