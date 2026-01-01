package dev.nyxane.mods.scalmyth.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class RoofBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final SideProperty SIDE = SideProperty.create("side");
    public static final BooleanProperty FULL = BooleanProperty.create("full");

    public RoofBlock(Properties properties) {
        super(properties);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockstate = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(FULL, false).setValue(SIDE, Side.NONE);
        return blockstate;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(SIDE).add(FULL);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    private VoxelShape getVoxelShape(BlockState state) {
        var direction = state.getValue(FACING);
        var full = state.getValue(FULL);
        var side = state.getValue(SIDE);

        if (full) {
            return switch (direction) {
                case NORTH, SOUTH -> Shapes.or(Block.box(0, 0, -6, 16, 8, 22), Block.box(0, 8, 0, 16, 16, 16));
                case WEST, EAST -> Shapes.or(Block.box(-6, 0, 0, 22, 8, 16), Block.box(0, 8, 0, 16, 16, 16));
                default -> Shapes.block();
            };
        } else {
            return switch (direction) {
                case NORTH -> Shapes.or(Block.box(0, 0, 14, 16, 8, 22), Block.box(0, 8, 5, 16, 16, 14),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 2, 16, 16);
                        case RIGHT -> Block.box(14, 0, 0, 16, 16, 16);
                    });
                case SOUTH -> Shapes.or(Block.box(0, 0, -6d, 16, 8, 2), Block.box(0, 8, 2, 16, 16, 11),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(14, 0, 0, 16, 16, 16);
                        case RIGHT -> Block.box(0, 0, 0, 2, 16, 16);
                    });
                case WEST -> Shapes.or(Block.box(14, 0, 0, 22, 8, 16), Block.box(5, 8, 0, 14, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 14, 16, 16, 16);
                        case RIGHT -> Block.box(0, 0, 0, 16, 16, 2);
                    });
                case EAST -> Shapes.or(Block.box(-6, 0, 0, 2, 8, 16), Block.box(2, 8, 0, 11, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 16, 16, 2);
                        case RIGHT -> Block.box(0, 0, 14, 16, 16, 16);
                    });
                default -> Shapes.block();
            };
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVoxelShape(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        var direction = state.getValue(FACING);
        var full = state.getValue(FULL);
        var side = state.getValue(SIDE);

        if (full) {
            return switch (direction) {
                case NORTH, SOUTH -> Shapes.or(
                    Block.box(0, 15, 0, 16, 16, 16),
                    Block.box(0, 0, 0, 16, 16, 1),
                    Block.box(0, 0, 15, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 1, 16, 16);
                        case RIGHT -> Block.box(15, 0, 0, 16, 16, 16);
                    });
                case WEST, EAST -> Shapes.or(
                    Block.box(0, 15, 0, 16, 16, 16),
                    Block.box(0, 0, 0, 1, 16, 16),
                    Block.box(15, 0, 0, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 16, 16, 1);
                        case RIGHT -> Block.box(0, 0, 15, 16, 16, 16);
                    });
                default -> Shapes.block();
            };
        } else {
            return switch (direction) {
                case NORTH -> Shapes.or(Block.box(0, 0, 15, 16, 16, 16), Block.box(0, 15, 0, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 2, 16, 16);
                        case RIGHT -> Block.box(14, 0, 0, 16, 16, 16);
                    });
                case SOUTH -> Shapes.or(Block.box(0, 0, 0, 16, 16, 1), Block.box(0, 15, 0, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(14, 0, 0, 16, 16, 16);
                        case RIGHT -> Block.box(0, 0, 0, 2, 16, 16);
                    });
                case WEST -> Shapes.or(Block.box(15, 0, 0, 16, 16, 16), Block.box(0, 15, 0, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 14, 16, 16, 16);
                        case RIGHT -> Block.box(0, 0, 0, 16, 16, 2);
                    });
                case EAST -> Shapes.or(Block.box(0, 0, 0, 1, 16, 16), Block.box(0, 15, 0, 16, 16, 16),
                    switch (side) {
                        case NONE -> Shapes.empty();
                        case LEFT -> Block.box(0, 0, 0, 16, 16, 2);
                        case RIGHT -> Block.box(0, 0, 14, 16, 16, 16);
                    });
                default -> Shapes.block();
            };
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public enum Side implements StringRepresentable {
        NONE("none"),
        LEFT("left"),
        RIGHT("right");

        final String name;

        Side(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static class SideProperty extends EnumProperty<Side> {
        protected SideProperty(String name, Collection<Side> values) {
            super(name, Side.class, values);
        }

        public static SideProperty create(String name) {
            return new SideProperty(name, Arrays.stream(Side.values()).collect(Collectors.toList()));
        }
    }
}
