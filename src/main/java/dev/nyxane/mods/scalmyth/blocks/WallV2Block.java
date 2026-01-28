package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallV2Block extends Block {
    public static BooleanProperty NORTH = BooleanProperty.create("north");
    public static BooleanProperty EAST = BooleanProperty.create("east");
    public static BooleanProperty SOUTH = BooleanProperty.create("south");
    public static BooleanProperty WEST = BooleanProperty.create("west");
    public static BooleanProperty UPDATE_SHAPE = BooleanProperty.create("update_shape");

    public WallV2Block(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UPDATE_SHAPE);
    }

    private VoxelShape getVoxelShape(BlockState state) {
        var north = state.getValue(NORTH);
        var east = state.getValue(EAST);
        var south = state.getValue(SOUTH);
        var west = state.getValue(WEST);

        return Shapes.or(
            north ? Block.box(0, 0, 0, 16, 16, 2) : Shapes.empty(),
            east ? Block.box(14, 0, 0, 16, 16, 16) : Shapes.empty(),
            south ? Block.box(0, 0, 14, 16, 16, 16) : Shapes.empty(),
            west ? Block.box(0, 0, 0, 2, 16, 16) : Shapes.empty(),
            !(north || east || south || west) ? Block.box(2, 2, 2, 14, 14, 14) : Shapes.empty()
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getVoxelShape(state);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.getValue(UPDATE_SHAPE)) return state;

        if (pos.north().equals(neighborPos)) {
            return state.setValue(NORTH, !neighborState.is(ModBlocks.WALL_V2));
        }
        if (pos.south().equals(neighborPos)) {
            return state.setValue(SOUTH, !neighborState.is(ModBlocks.WALL_V2));
        }
        if (pos.east().equals(neighborPos)) {
            return state.setValue(EAST, !neighborState.is(ModBlocks.WALL_V2));
        }
        if (pos.west().equals(neighborPos)) {
            return state.setValue(WEST, !neighborState.is(ModBlocks.WALL_V2));
        }
        return state;
    }
}
