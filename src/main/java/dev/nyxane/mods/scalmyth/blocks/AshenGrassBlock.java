package dev.nyxane.mods.scalmyth.blocks;

import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.util.DeferredSoundType;
import dev.nyxane.mods.scalmyth.block_operations.AshenGrassBonemeal;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.Mth;

public class AshenGrassBlock extends Block implements BonemealableBlock {
    public AshenGrassBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                .sound(new DeferredSoundType(1.0f, 1.0f, () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.break")), () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.place")), () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.mine")),
                        () -> BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("scalmyth:ashen.earth.step"))))
                .strength(0.6f).randomTicks());
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 15;
    }

    @Override
    public void randomTick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.randomTick(blockstate, world, pos, random);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        double goalX = pos.getX();
        double goalY = pos.getY();
        double goalZ = pos.getZ();
        if (world.getBlockState(BlockPos.containing(x, y + 1, z)).canOcclude()) {
            world.setBlock(BlockPos.containing(x, y, z), Blocks.DIRT.defaultBlockState(), 3);
        } else {
            goalX = x + Mth.nextInt(RandomSource.create(), -1, 1);
            goalY = y + Mth.nextInt(RandomSource.create(), -1, 1);
            goalZ = z + Mth.nextInt(RandomSource.create(), -1, 1);
            if ((world.getBlockState(BlockPos.containing(goalX, goalY, goalZ))).getBlock() == Blocks.DIRT) {
                if (!world.getBlockState(BlockPos.containing(goalX, goalY + 1, goalZ)).canOcclude()) {
                    world.setBlock(BlockPos.containing(goalX, goalY, goalZ), dev.nyxane.mods.scalmyth.registry.Blocks.ASHEN_GRASS.get().defaultBlockState(), 3);
                }
            }
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