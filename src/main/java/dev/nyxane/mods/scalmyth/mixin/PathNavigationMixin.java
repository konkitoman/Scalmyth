package dev.nyxane.mods.scalmyth.mixin;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Mixin(PathNavigation.class)
public abstract class  PathNavigationMixin {
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

/*    @Shadow
    private boolean shouldTargetNextNodeInDirection(Vec3 vec) {
        throw new RuntimeException("A shadow method was called");
    }*/

  @Shadow
  protected abstract boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32);

/*  @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/Path;getNextEntityPos(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 tick(Vec3 original) {
        return original.subtract(mob.getBbWidth() / 2, 0, mob.getBbWidth() / 2);
    }*/

    @Shadow
    @Nullable
    protected abstract Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy);

    @Shadow
    protected abstract boolean canUpdatePath();

    @Shadow
    @Final
    protected Level level;

    @Shadow
    @Final
    private PathFinder pathFinder;

    @Shadow
    private float maxVisitedNodesMultiplier;

    @Shadow
    @Nullable
    private BlockPos targetPos;

    @Shadow
    private int reachRange;

    @Shadow
    protected abstract void resetStuckTimeout();

    @Shadow
    protected abstract boolean shouldTargetNextNodeInDirection(Vec3 vec);

    @Inject(method = "followThePath", at = @At("HEAD"), cancellable = true)
    private void followThePath(CallbackInfo ci) throws NoSuchFieldException, IllegalAccessException {
        {
            Node[] nodes = new Node[path.getNodeCount()];
            for (int i = 0; i < path.getNodeCount(); i++) {
                nodes[i] = path.getNode(i);
            }

            HashSet<Target> targets = new HashSet<Target>(1);
            targets.add(new Target(path.getNextNode()));

            Path.DebugData data = new Path.DebugData(nodes, new Node[0], targets);
            Field Path$debugData = Path.class.getDeclaredField("debugData");
            Path$debugData.setAccessible(true);
            Path$debugData.set(path, data);
            if (path.debugData() != null && mob.level() instanceof ServerLevel) {
                PathfindingDebugPayload payload = new PathfindingDebugPayload(mob.getId(), path, 10);
                for (ServerPlayer player : ((ServerLevel) this.mob.level()).players()) {
                    player.connection.send(new ClientboundCustomPayloadPacket(payload));
                }
            }
        }

        //[16:24:27] [Server thread/INFO] [de.ny.mo.sc.ap.ScalmythAPI/]: P: -3686.4963818201095 169.0 4025.509035701155
        //[16:24:27] [Server thread/INFO] [de.ny.mo.sc.ap.ScalmythAPI/]: T: -3686.5 169.0 4025.5
        //[16:24:27] [Server thread/INFO] [de.ny.mo.sc.ap.ScalmythAPI/]: maxDistance: 0.45, d: 1.003618179890509, 1.0090357011549713

        Vec3 vec3 = getTempMobPos();
        maxDistanceToWaypoint = mob.getBbWidth()/2.0F;
        Vec3i vec3i = path.getNextNodePos();
        double d0 = Math.abs(mob.getX() - ((double)vec3i.getX() + 0.5));
        double d1 = Math.abs(mob.getY() - (double)vec3i.getY());
        double d2 = Math.abs(mob.getZ() - ((double)vec3i.getZ() + 0.5));
        ScalmythAPI.LOGGER.info("P: {} {} {}", this.mob.getX(), this.mob.getY(), this.mob.getZ());
        ScalmythAPI.LOGGER.info("T: {} {} {}", ((double)vec3i.getX() + 0.5), ((double)vec3i.getY()), ((double)vec3i.getZ() + 0.5));
        ScalmythAPI.LOGGER.info("maxDistance: {}, d: {}, {}", maxDistanceToWaypoint, d0, d2);
        boolean flag = d0 < this.maxDistanceToWaypoint && d2 < this.maxDistanceToWaypoint && d1 < 1.0;
        if (flag || this.canCutCorner(this.path.getNextNode().type) && shouldTargetNextNodeInDirection(vec3)) {
            this.path.advance();
        }

        doStuckDetection(vec3);
        ci.cancel();
    }


/*    @Overwrite
    public Path createPath(BlockPos pos, int accuracy) {
        ScalmythAPI.LOGGER.info("Create path");
        return createPath(ImmutableSet.of(pos), 1, false, accuracy);
    }*/
}
