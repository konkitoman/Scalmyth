package dev.nyxane.mods.scalmyth.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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

    public RoofSlab(Properties properties) {
        super(properties);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var blockpos = context.getClickedPos();
        var blockstate = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(VARIANT, Variant.LOWER).setValue(TOP, false);

        return context.getClickLocation().y - (double) blockpos.getY() > (double) 0.5F ? blockstate.setValue(VARIANT, Variant.UPPER) : blockstate;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(VARIANT).add(TOP);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var variant = state.getValue(VARIANT);

        return switch (variant) {
            case LOWER -> Block.box(0, 0, 0, 16, 8, 16);
            case UPPER -> Shapes.or(Block.box(0, 8, 0, 16, 16, 16));
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
        LOWER("lower"),
        UPPER("upper");

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
}
