package dev.nyxane.mods.scalmyth.block_operations;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class AshenGrassBonemeal{
    public static void useOn(LevelAccessor world, BlockPos pos) {
        BlockPos checkPos = BlockPos.containing(pos.getX(), pos.getY() + 1, pos.getZ());
        double x = checkPos.getX();
        double y = checkPos.getX();
        double z = checkPos.getX();
        double goalX = checkPos.getX();
        double goalY = checkPos.getY();
        double goalZ = checkPos.getZ();

        for (int i = 0; i < 5; i++) {
            goalX = x + Mth.nextInt(RandomSource.create(), -1, 1);
            goalY = y + Mth.nextInt(RandomSource.create(), -1, 1);
            goalZ = z + Mth.nextInt(RandomSource.create(), -1, 1);
            if ((world.getBlockState(BlockPos.containing(goalX, goalY - 1, goalZ))).getBlock() == dev.nyxane.mods.scalmyth.registry.Blocks.ASHEN_GRASS.get()) {
                if (world.getBlockState(BlockPos.containing(goalX, goalY, goalZ)).getBlock() == Blocks.AIR) {
                    world.setBlock(BlockPos.containing(goalX, goalY, goalZ), dev.nyxane.mods.scalmyth.registry.Blocks.ASHEN_SHORT_GRASS.get().defaultBlockState(), 3);
                }
            }
        }
    }
}