package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "followThePath", at = @At("HEAD"), cancellable = true)
    private void followThePath(CallbackInfo ci) {
        Vec3 vec3 = getTempMobPos();
        maxDistanceToWaypoint = mob.getBbWidth() / 2.0F;
        Vec3 nPos = path.getNextEntityPos(mob);
        double d0 = Math.abs(mob.getX() - nPos.x);
        double d1 = Math.abs(mob.getY() - nPos.y);
        double d2 = Math.abs(mob.getZ() - nPos.z);
        boolean flag = d0 < this.maxDistanceToWaypoint && d2 < this.maxDistanceToWaypoint && d1 < 1.0;
        if (flag || this.canCutCorner(this.path.getNextNode().type) && shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        doStuckDetection(vec3);
        ci.cancel();
    }
}
