package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import dev.nyxane.mods.scalmyth.block_operations.AshenGrassBonemeal;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public class AshenShortGrassBlock extends FlowerBlock implements BonemealableBlock {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public AshenShortGrassBlock() {
        super(MobEffects.MOVEMENT_SPEED, 100,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK)
                        .sound(new DeferredSoundType(1.0f, 1.0f,
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.break")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.place")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.mine")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step"))))
                        .instabreak()
                        .noCollission()
                        .replaceable()
                        .ignitedByLava()
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .pushReaction(PushReaction.DESTROY));

        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.SHORT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public PathType getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) {
        return PathType.OPEN;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 30;
    }

    @Override
    public boolean mayPlaceOn(BlockState groundState, BlockGetter worldIn, BlockPos pos) {
        return groundState.is(ModBlocks.ASHEN_GRASS.get());
    }

    @Override
    public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).getBlock() == ModBlocks.ASHEN_GRASS.get();
    }

    @Override
    public void neighborChanged(BlockState blockstate, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        if (world.getBlockState(pos.below()).getBlock() != ModBlocks.ASHEN_GRASS.get()) {
            world.destroyBlock(pos, false);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader worldIn, BlockPos pos, BlockState blockstate) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState blockstate) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState blockstate) {
        AshenGrassBonemeal.useOn(world, pos);
    }

    public enum Variant implements StringRepresentable {
        SHORT("short"),
        TALL("tall");

        private final String name;
        Variant(String name) { this.name = name; }
        @Override
        public String getSerializedName() { return name; }
    }
}