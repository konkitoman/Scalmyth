package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {
    @Shadow
    protected Vec3 getTempMobPos() {
        throw new RuntimeException("A shadow method was called");
    }

    @Shadow
    protected void doStuckDetection(Vec3 vec3) {
        throw new RuntimeException("A shadow method was called");
    }

    @Final
    @Shadow
    protected Mob mob;

    @Shadow
    protected float maxDistanceToWaypoint;
    @Shadow
    protected Path path;

    @Shadow
    public boolean canCutCorner(PathType pathType) {
        throw new RuntimeException("A shadow method was called");
    }

    @Shadow
    @Final
    protected Level level;

    @Shadow
    protected abstract boolean shouldTargetNextNodeInDirection(Vec3 vec);

    @Overwrite
    public void followThePath() {
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        while(path.getNextNodeIndex() < path.getNodeCount()){
            Vec3 next = this.path.getNextEntityPos(mob);
            double d0 = Math.abs(this.mob.getX() - next.x);
            double d1 = Math.abs(this.mob.getY() - next.y);
            double d2 = Math.abs(this.mob.getZ() - next.z);
            boolean flag = d0 < (double)this.maxDistanceToWaypoint && d2 < (double)this.maxDistanceToWaypoint && d1 < 1.0D;
            if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
                this.path.advance();
                continue;
            }
            break;
        }

        this.doStuckDetection(vec3);
    }
}
