package dev.nyxane.mods.scalmyth.mixin.vanilla;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {
    @Shadow
    protected abstract Vec3 getTempMobPos();

    @Shadow
    protected float maxDistanceToWaypoint;

    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    public abstract boolean canCutCorner(PathType pathType);

    @Shadow
    @Nullable
    protected Path path;

    @Shadow
    protected abstract void doStuckDetection(Vec3 positionVec3);

    @Shadow
    protected abstract boolean shouldTargetNextNodeInDirection(Vec3 vec);

    @Overwrite()
    public void followThePath() {
        Vec3 vec3 = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i vec3i = this.path.getNextNodePos();
        double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + 0.5D));
        double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + 0.5D));
        boolean flag = d0 < (double)this.maxDistanceToWaypoint && d2 < (double)this.maxDistanceToWaypoint && d1 < 1.0D;
        if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        this.doStuckDetection(vec3);
    }
}
