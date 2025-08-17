package dev.nyxane.mods.scalmyth.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumSet;
import java.util.Set;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin extends NodeEvaluator {

    @Overwrite
    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

        for(int i = 0; i < entityWidth; ++i) {
            for(int j = 0; j < entityHeight; ++j) {
                for(int k = 0; k < entityDepth; ++k) {
                    int l = (i + x) - ((entityWidth > 4) ? (entityWidth/2) : 0);
                    int i1 = j + y;
                    int j1 = (k + z) - ((entityWidth > 4) ? (entityWidth/2) : 0);
                    PathType pathtype = this.getPathType(context, l, i1, j1);
                    BlockPos blockpos = this.mob.blockPosition();
                    boolean flag = this.canPassDoors();
                    if (pathtype == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && flag) {
                        pathtype = PathType.WALKABLE_DOOR;
                    }

                    if (pathtype == PathType.DOOR_OPEN && !flag) {
                        pathtype = PathType.BLOCKED;
                    }

                    if (pathtype == PathType.RAIL && this.getPathType(context, blockpos.getX(), blockpos.getY(), blockpos.getZ()) != PathType.RAIL && this.getPathType(context, blockpos.getX(), blockpos.getY() - 1, blockpos.getZ()) != PathType.RAIL) {
                        pathtype = PathType.UNPASSABLE_RAIL;
                    }

                    enumset.add(pathtype);
                }
            }
        }

        return enumset;
    }
}
