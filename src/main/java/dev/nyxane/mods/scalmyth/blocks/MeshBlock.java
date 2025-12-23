package dev.nyxane.mods.scalmyth.blocks;

import com.mojang.serialization.MapCodec;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MeshBlock extends BaseEntityBlock {
    public static final MapCodec<MeshBlock> CODEC = simpleCodec(MeshBlock::new);
    public MeshBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MeshBlockEntity(blockPos, blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var o_meshBlockEntity = level.getBlockEntity(pos, ModBlocks.MESH_ENTITY.get());

        if (o_meshBlockEntity.isEmpty()) return Shapes.box(0.2, 0.2, 0.2, 0.8, 0.8, 0.8);
        return o_meshBlockEntity.get().getShape();
    }
}
