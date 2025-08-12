package dev.nyxane.mods.scalmyth.block_operations;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class AshenGrassBonemeal{
    public static void useOn(LevelAccessor world, BlockPos pos) {
        BlockPos checkPos = BlockPos.containing(pos.getX(), pos.getY(), pos.getZ());
        double x = checkPos.getX();
        double y = checkPos.getY();
        double z = checkPos.getZ();
        double goalX = checkPos.getX();
        double goalY = checkPos.getY();
        double goalZ = checkPos.getZ();

        //ScalmythAPI.LOGGER.info("bonemeal used on ashen grass type at x"+x+" y"+y+" z"+z);
        for (int i = 0; i < 10; i++) {
            goalX = x + Mth.nextInt(RandomSource.create(), -1, 1);
            goalY = y + Mth.nextInt(RandomSource.create(), -2, 1);
            goalZ = z + Mth.nextInt(RandomSource.create(), -1, 1);
            if ((world.getBlockState(BlockPos.containing(goalX, goalY - 1, goalZ))).getBlock() == ModBlocks.ASHEN_GRASS.get()) {
                if (world.getBlockState(BlockPos.containing(goalX, goalY, goalZ)).getBlock().defaultBlockState().isAir()) {
                    world.setBlock(BlockPos.containing(goalX, goalY, goalZ), ModBlocks.ASHEN_SHORT_GRASS.get().defaultBlockState(), 3);
                }
            }
        }
    }
}