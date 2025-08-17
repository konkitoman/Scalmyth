package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(Path.class)
public class PathMixin {
    @Shadow
    @Final
    private List<Node> nodes;

    @Overwrite
    public Vec3 getEntityPosAtNode(Entity entity, int index) {
        Node node = (Node) this.nodes.get(index);
        double d0 = (double) node.x + ((entity.getBbWidth() / 2) % 1);
        double d1 = (double) node.y;
        double d2 = (double) node.z + ((entity.getBbWidth() / 2) % 1);
        return new Vec3(d0, d1, d2);
    }
}
