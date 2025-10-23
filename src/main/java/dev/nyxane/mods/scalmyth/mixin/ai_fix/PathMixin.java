package dev.nyxane.mods.scalmyth.mixin.ai_fix;

import dev.nyxane.mods.scalmyth.KDebug;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
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
        int max_offset = (int)Math.round((entity.getBbWidth()) / 2);
        int x_offset = max_offset;
        int z_offset = x_offset;

        int id_offset = 2;

        int max = (int) Math.round(entity.getBbWidth());
        for (int y = 0; y < (int)Math.ceil(entity.getBbHeight()); y++) {
            for (int ii = 0; ii <= max; ii++) {
                for (int i = 0; i < max_offset; i++) {
                    BlockPos pos = node.asBlockPos().offset(-(i + 1), y, ii);
//                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
//                        .setColor(0xff0000ff)
//                        .setId(entity.hashCode() + id_offset));
//                    id_offset += 1;

                    BlockState state = entity.level().getBlockState(pos);
                    if (state.isSolid()) {
                        x_offset = Math.min(x_offset, i);
                        break;
                    }
                }
                for (int i = 0; i < max_offset; i++) {
                    BlockPos pos = node.asBlockPos().offset(ii, y, -(i + 1));
//                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
//                        .setColor(0xffff0000)
//                        .setId(entity.hashCode() + id_offset));
//                    id_offset += 1;
                    BlockState state = entity.level().getBlockState(pos);
                    if (state.isSolid()) {
                        z_offset = Math.min(z_offset, i);
                        break;
                    }
                }
            }
        }

        x_offset = Math.max(0, max_offset - x_offset);
        z_offset = Math.max(0, max_offset - z_offset);

        if (node.y < entity.getBlockY()) {
            int found_x = 0;
            int found_z = 0;
            for (int i = 0; i < max_offset; i++) {
                for (int ii = 0; ii < max_offset; ii++) {
                    BlockPos pos = node.asBlockPos().offset(-(ii), (entity.getBlockY() - 1) - node.y, -(i));
//                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
//                        .setColor(0xffffffff)
//                        .setTime(10)
//                        .setId(entity.hashCode() + id_offset));
//                    id_offset += 1;
                    BlockState state = entity.level().getBlockState(pos);
                    if (state.isSolid()) {
                        found_x = Math.max(ii+1, found_x);
                        found_z = Math.max(i+1, found_z);
                    }
                }
            }
            if (found_x != 0) x_offset = Math.max(x_offset, found_x);
            if (found_z != 0) z_offset = Math.max(z_offset, found_z);
            ScalmythAPI.LOGGER.info("Found Down: {} {}", found_x, found_z);
        }

        if (node.y > entity.getBlockY()) {
            int found_x = max;
            int found_z = found_x;
            for (int i = 0; i <= max; i++) {
                for (int ii = 0; ii <= max; ii++) {
                    if (i <= max_offset && ii <= max_offset){
                        continue;
                    }
                    BlockPos pos;
                    BlockState state;
                    pos = node.asBlockPos().below().offset(i, 0, ii);
//                    KDebug.addShape(entity.level(), new KDebug.Shape.Box(Vec3.atBottomCenterOf(pos).add(0, 0.5, 0), new Vec3(0.5, 0.5, 0.5))
//                        .setColor(0xffffff00)
//                        .setTime(10)
//                        .setId(entity.hashCode() + id_offset));
//                    id_offset += 1;
                    state = entity.level().getBlockState(pos);

                    if (state.isSolid()) {
                        found_x = Math.max(0, i - max_offset);
                        found_z = Math.max(0, ii - max_offset);
                        break;
                    }

                    if (found_x != max || found_z != max) {
                        break;
                    }
                }
            }
            if (found_x != max) x_offset = Math.max(x_offset, found_x - 1);
            if (found_z != max) z_offset = Math.max(z_offset, found_z - 1);
            ScalmythAPI.LOGGER.info("Found Up: {} {}", found_x, found_z);
        }


        double d0 = (double) node.x + Math.min(0.5 + (double)x_offset, ((int)entity.getBbWidth() + 1.0) * 0.5);
        double d1 = (double) node.y;
        double d2 = (double) node.z + Math.min(0.5 + (double)z_offset, ((int)entity.getBbWidth() + 1.0) * 0.5);
        return new Vec3(d0, d1, d2);
    }
}
