package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import dev.nyxane.mods.scalmyth.KDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
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
        int already_counted = Math.max(0, (int)Math.round((entity.getBbWidth()-1.0)/2));
        Node node = (Node) this.nodes.get(index);
        int max_offset = (int)Math.round((entity.getBbWidth()) / 2) - already_counted;
        int x_offset = max_offset;
        int z_offset = x_offset;

        int id_offset = 3;

        int max = (int) Math.round(entity.getBbWidth()) - already_counted;
        for (int y = 0; y < (int)Math.ceil(entity.getBbHeight()); y++) {
            for (int ii = 0; ii < max; ii++) {
                for (int i = 0; i < max_offset; i++) {
                    BlockPos pos = node.asBlockPos().offset(-(i + 1), y, ii);
                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
                        .setColor(0xff0000ff)
                        .setId(entity.hashCode() + id_offset));
                    id_offset += 1;

                    BlockState state = entity.level().getBlockState(pos);
                    if (state.isSolid()) {
                        x_offset = Math.min(x_offset, i);
                    }
                }
                for (int i = 0; i < max_offset; i++) {
                    BlockPos pos = node.asBlockPos().offset(ii, y, -(i + 1));
                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
                        .setColor(0xffff0000)
                        .setId(entity.hashCode() + id_offset));
                    id_offset += 1;
                    BlockState state = entity.level().getBlockState(pos);
                    if (state.isSolid()) {
                        z_offset = Math.min(z_offset, i);
                    }
                }
            }
        }

        x_offset = Math.max(0, max_offset - x_offset);
        z_offset = Math.max(0, max_offset - z_offset);

        double d0 = (double) node.x + Math.min(0.5 + (double)x_offset, (((int)entity.getBbWidth() + 1.0) - already_counted) * 0.5);
        double d1 = (double) node.y;
        double d2 = (double) node.z + Math.min(0.5 + (double)z_offset, (((int)entity.getBbWidth() + 1.0) - already_counted) * 0.5);
        return new Vec3(d0, d1, d2);
    }
}
