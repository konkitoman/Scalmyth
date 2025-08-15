package dev.nyxane.mods.scalmyth.block_operations;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BloodFlowerBonemeal {
    public static void useOn(LevelAccessor world, RandomSource rand, @NotNull BlockPos pos, BlockState originalState) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                if(dx == 0 && dz == 0) continue;

                int newX = x+dx;
                int newZ = z+dz;

                BlockPos candidatePos = BlockPos.containing(newX, y, newZ);
                BlockState candidateState = world.getBlockState(candidatePos);

                if(!candidateState.isEmpty()) continue;

                BlockState underCandidateState = world.getBlockState(candidatePos.below());

                if(!underCandidateState.is(ModBlocks.ASHEN_GRASS.get())) continue;

                if(rand.nextBoolean()) {
                    // idrk the right flags
                    world.setBlock(candidatePos, originalState, 3);
                }
            }
        }
    }
}
