package dev.nyxane.mods.scalmyth.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.HashSet;

@Mixin(DebugPackets.class)
public class DebugPacketsMixin {
    @Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
    private static void sendPathFindingPacket(Level level, Mob mob, Path path, float maxDistanceToWaypoint, CallbackInfo ci) throws IllegalAccessException, NoSuchFieldException {
        if (path == null) return;

        if (level instanceof ServerLevel serverLevel){
            
            Node[] closed = new Node[path.getNextNodeIndex()];
            for (int i = 0; i < path.getNextNodeIndex(); i++) {
                closed[i] = path.getNode(i);
            }
            Node[] open = new Node[path.getNodeCount() - path.getNextNodeIndex()];
            for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
                open[i-path.getNextNodeIndex()] = path.getNode(i);
            }

            HashSet<Target> targets = new HashSet<Target>(1);
            BlockPos target = path.getTarget();
            targets.add(new Target(target.getX(), target.getY(), target.getZ()));

            Path.DebugData data = new Path.DebugData(open, closed, targets);
            Field Path$debugData = Path.class.getDeclaredField("debugData");
            Path$debugData.setAccessible(true);
            Path$debugData.set(path, data);
            if (path.debugData() != null) {
                PathfindingDebugPayload payload = new PathfindingDebugPayload(mob.getId(), path, 10);
                for (ServerPlayer player : (serverLevel).players()) {
                    player.connection.send(new ClientboundCustomPayloadPacket(payload));
                }
            }
        }
    }
}
