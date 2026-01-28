package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class RoofSlab extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VariantProperty VARIANT = VariantProperty.create("variant");
    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty SIDE_LEFT = BooleanProperty.create("side_left");
    public static final BooleanProperty SIDE_RIGHT = BooleanProperty.create("side_right");

    public RoofSlab(Properties properties) {
        super(properties);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockpos = context.getClickedPos();
        var blockstate = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(VARIANT, Variant.LOWER).setValue(TOP, false).setValue(SIDE_LEFT, false).setValue(SIDE_RIGHT, false);

        return updateSelf(context.getLevel(), blockpos, context.getClickLocation().y - (double) blockpos.getY() > (double) 0.5F ? blockstate.setValue(VARIANT, Variant.UPPER) : blockstate);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT, TOP, SIDE_LEFT, SIDE_RIGHT);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var facing = state.getValue(FACING);
        var variant = state.getValue(VARIANT);
        var side_left = state.getValue(SIDE_LEFT);
        var side_right = state.getValue(SIDE_RIGHT);


        return switch (variant) {
            case LOWER -> Block.box(0, 0, 0, 16, 8, 16);
            case UPPER -> Shapes.or(Block.box(0, 8, 0, 16, 16, 16), side_left ? switch (facing) {
                case NORTH -> Block.box(0, 0, 0, 2, 16, 16);
                case EAST -> Block.box(0, 0, 0, 16, 16, 2);
                case SOUTH -> Block.box(14, 0, 0, 16, 16, 16);
                case WEST -> Block.box(0, 0, 14, 16, 16, 16);
                default -> Shapes.empty();
            } : Shapes.empty(), side_right ? switch (facing) {
                case NORTH -> Block.box(14, 0, 0, 16, 16, 16);
                case EAST -> Block.box(0, 0, 14, 16, 16, 16);
                case SOUTH -> Block.box(0, 0, 0, 2, 16, 16);
                case WEST -> Block.box(0, 0, 0, 16, 16, 2);
                default -> Shapes.empty();
            } : Shapes.empty());
        };
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public enum Variant implements StringRepresentable {
        LOWER("lower"), UPPER("upper");

        final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class VariantProperty extends EnumProperty<RoofSlab.Variant> {
        protected VariantProperty(String name, Collection<RoofSlab.Variant> values) {
            super(name, RoofSlab.Variant.class, values);
        }

        public static RoofSlab.VariantProperty create(String name) {
            return new RoofSlab.VariantProperty(name, Arrays.stream(RoofSlab.Variant.values()).collect(Collectors.toList()));
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateSelf(level, pos, state);
    }

    private BlockState updateSelf(LevelAccessor level, BlockPos pos, BlockState state) {
        var west = level.getBlockState(pos.west());
        var east = level.getBlockState(pos.east());
        var north = level.getBlockState(pos.north());
        var south = level.getBlockState(pos.south());

        if (west.is(ModBlocks.ROOF_SLAB) && east.is(ModBlocks.ROOF_SLAB) &&
            west.getValue(VARIANT) == Variant.LOWER && east.getValue(VARIANT) == Variant.LOWER &&
            west.getValue(FACING) == Direction.EAST && east.getValue(FACING) == Direction.WEST) {
            return state.setValue(VARIANT, Variant.UPPER).setValue(TOP, true).setValue(FACING, Direction.EAST);
        }

        if (north.is(ModBlocks.ROOF_SLAB) && south.is(ModBlocks.ROOF_SLAB) &&
            north.getValue(VARIANT) == Variant.LOWER && south.getValue(VARIANT) == Variant.LOWER &&
            north.getValue(FACING) == Direction.SOUTH && south.getValue(FACING) == Direction.NORTH) {
            return state.setValue(VARIANT, Variant.UPPER).setValue(TOP, true).setValue(FACING, Direction.NORTH);
        }

        var below_west = level.getBlockState(pos.below().west());
        var below_east = level.getBlockState(pos.below().east());
        var below_north = level.getBlockState(pos.below().north());
        var below_south = level.getBlockState(pos.below().south());

        if (below_west.is(ModBlocks.ROOF_SLAB) && below_east.is(ModBlocks.ROOF_SLAB) &&
            below_west.getValue(VARIANT) == Variant.UPPER && below_east.getValue(VARIANT) == Variant.UPPER &&
            below_west.getValue(FACING) == Direction.EAST && below_east.getValue(FACING) == Direction.WEST) {
            return state.setValue(VARIANT, Variant.LOWER).setValue(TOP, true).setValue(FACING, Direction.EAST);
        }

        if (below_north.is(ModBlocks.ROOF_SLAB) && below_south.is(ModBlocks.ROOF_SLAB) &&
            below_north.getValue(VARIANT) == Variant.UPPER && below_south.getValue(VARIANT) == Variant.UPPER &&
            below_north.getValue(FACING) == Direction.SOUTH && below_south.getValue(FACING) == Direction.NORTH) {
            return state.setValue(VARIANT, Variant.LOWER).setValue(TOP, true).setValue(FACING, Direction.NORTH);
        }

        return state;
    }
}
