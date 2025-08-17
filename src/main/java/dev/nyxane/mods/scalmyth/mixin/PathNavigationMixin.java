package dev.nyxane.mods.scalmyth.mixin;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;

@Mixin(PathNavigation.class)
public class PathNavigationMixin {

    @Shadow
    @Nullable
    protected Path path;

    @Shadow
    @Final
    protected Mob mob;

    @Inject(method = "followThePath", at = @At("HEAD"))
    void fallowThePath(CallbackInfo cir) throws NoSuchFieldException, IllegalAccessException {
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
}
