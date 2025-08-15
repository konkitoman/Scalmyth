package dev.nyxane.mods.scalmyth.block_operations;

import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AshenGrassBonemeal{
    public static void useOn(LevelAccessor world, BlockPos pos, RandomSource rand) {
        BlockPos checkPos = BlockPos.containing(pos.getX(), pos.getY(), pos.getZ());
        double x = checkPos.getX();
        double y = checkPos.getY();
        double z = checkPos.getZ();
        double goalX;
        double goalY;
        double goalZ;

        //ScalmythAPI.LOGGER.info("bonemeal used on ashen grass type at x"+x+" y"+y+" z"+z);
        for (int i = 0; i < 10; i++) {
            goalX = x + rand.nextInt(-1, 1);
            goalY = y + rand.nextInt(-2, 1);
            goalZ = z + rand.nextInt(-1, 1);
            if ((world.getBlockState(BlockPos.containing(goalX, goalY - 1, goalZ))).getBlock() == ModBlocks.ASHEN_GRASS.get()) {
                if (world.getBlockState(BlockPos.containing(goalX, goalY, goalZ)).getBlock().defaultBlockState().isAir()) {
                    BlockState newBlock = rand.nextDouble() <= 0.75 ? ModBlocks.ASHEN_SHORT_GRASS.get().defaultBlockState() :
                            ModBlocks.BLOOD_FLOWER.get().defaultBlockState();
                    world.setBlock(BlockPos.containing(goalX, goalY, goalZ), newBlock, 3);
                }
            }
        }
    }
}