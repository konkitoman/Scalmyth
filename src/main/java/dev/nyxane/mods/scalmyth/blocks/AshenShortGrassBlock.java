package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.registry.Blocks;

import net.neoforged.neoforge.common.util.DeferredSoundType;
import dev.nyxane.mods.scalmyth.block_operations.AshenGrassBonemeal;

import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class AshenShortGrassBlock extends FlowerBlock implements BonemealableBlock {
    public AshenShortGrassBlock() {
        super(MobEffects.MOVEMENT_SPEED, 100,
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                        .sound(new DeferredSoundType(1.0f, 1.0f, () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.break")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step")), () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.place")),
                                () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.mine")), () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step"))))
                        .instabreak().noCollission().replaceable().ignitedByLava().offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));
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
        boolean additionalCondition = true;
        if (worldIn instanceof LevelAccessor world) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            additionalCondition = ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Blocks.ASHEN_GRASS.get()
                    && world.isEmptyBlock(BlockPos.containing(x, y, z))
            );
        }
        return groundState.is(Blocks.ASHEN_GRASS.get()) && additionalCondition;
    }

    @Override
    public boolean canSurvive(BlockState blockstate, LevelReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState groundState = worldIn.getBlockState(blockpos);
        return this.mayPlaceOn(groundState, worldIn, blockpos);
    }

    @Override
    public void neighborChanged(BlockState blockstate, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(blockstate, world, pos, neighborBlock, fromPos, moving);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (!((world.getBlockState(BlockPos.containing(x, y - 1, z))).getBlock() == Blocks.ASHEN_GRASS.get())) {
            world.destroyBlock(BlockPos.containing(x, y, z), false);
            world.setBlock(BlockPos.containing(x, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
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
        AshenGrassBonemeal.useOn(world,pos);
    }
}