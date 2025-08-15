package dev.nyxane.mods.scalmyth.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigation.class)
public class PathNavigationMixin {
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
    private boolean shouldTargetNextNodeInDirection(Vec3 vec) {
        throw new RuntimeException("A shadow method was called");
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/Path;getNextEntityPos(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 tick(Vec3 original) {
        return original.subtract(mob.getBbWidth() / 2, 0, mob.getBbWidth() / 2);
    }

    @Inject(method = "followThePath", at = @At("HEAD"), cancellable = true)
    private void followThePath(CallbackInfo ci) {
        Vec3 vec3 = getTempMobPos();
        maxDistanceToWaypoint = mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i vec3i = path.getNextNodePos();
        double d0 = Math.abs(mob.getX() - ((double)vec3i.getX() + 0.5F));
        double d1 = Math.abs(mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(mob.getZ() - ((double)vec3i.getZ() + 0.5F));
        boolean flag = d0 < this.maxDistanceToWaypoint && d2 < this.maxDistanceToWaypoint && d1 < 1.0;
        if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        doStuckDetection(vec3);
        ci.cancel();
    }
}
