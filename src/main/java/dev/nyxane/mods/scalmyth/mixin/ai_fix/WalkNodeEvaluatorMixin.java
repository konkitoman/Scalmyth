package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.EnumSet;
import java.util.Set;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin extends NodeEvaluator {

    @Overwrite
    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        EnumSet<PathType> enumset = EnumSet.noneOf(PathType.class);

        for (int i = 0; i < this.entityWidth; i++) {
            for (int j = 0; j < this.entityHeight; j++) {
                for (int k = 0; k < this.entityDepth; k++) {
                    int l = i + x - Math.max(0, (int) Math.floor((this.entityWidth - 1.0) / 2.0));
                    int i1 = j + y;
                    int j1 = k + z - Math.max(0, (int) Math.floor((this.entityDepth - 1.0) / 2.0));
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

        boolean no_center_x = entityWidth % 2 == 0;
        boolean no_center_z = entityDepth % 2 == 0;

        if (no_center_x){
            PathType pathtype = this.getPathType(context, x-1, y, z);
            if (pathtype == PathType.WALKABLE){
                enumset.add(PathType.WALKABLE);
            }
        }

        if (no_center_z){
            PathType pathtype = this.getPathType(context, x, y, z-1);
            if (pathtype == PathType.WALKABLE){
                enumset.add(PathType.WALKABLE);
            }
        }

        if (no_center_x && no_center_z){
            PathType pathtype = this.getPathType(context, x - 1, y, z - 1);
            if (pathtype == PathType.WALKABLE){
                enumset.add(PathType.WALKABLE);
            }
        }

        return enumset;
    }
}
