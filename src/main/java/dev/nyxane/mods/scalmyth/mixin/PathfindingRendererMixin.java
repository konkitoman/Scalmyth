package dev.nyxane.mods.scalmyth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.PathfindingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(PathfindingRenderer.class)
public abstract class PathfindingRendererMixin {
    @Shadow
    public static void renderPathLine(PoseStack poseStack, VertexConsumer consumer, Path path, double x, double y, double z) {
    }

    @Shadow
    protected static float distanceToCamera(BlockPos pos, double x, double y, double z) {
        return 0;
    }

    @Overwrite
    public static void renderPath(PoseStack poseStack, MultiBufferSource buffer, Path path, float p_270841_, boolean p_270481_, boolean p_270748_, double x, double y, double z) {
        p_270841_ = 0.5F;
        renderPathLine(poseStack, buffer.getBuffer(RenderType.debugLineStrip((double)6.0F)), path, x, y, z);
        BlockPos blockpos = path.getTarget();
        if (distanceToCamera(blockpos, x, y, z) <= 80.0F) {
            DebugRenderer.renderFilledBox(poseStack, buffer, (new AABB((double)((float)blockpos.getX() + 0.25F), (double)((float)blockpos.getY() + 0.25F), (double)blockpos.getZ() + (double)0.25F, (double)((float)blockpos.getX() + 0.75F), (double)((float)blockpos.getY() + 0.75F), (double)((float)blockpos.getZ() + 0.75F))).move(-x, -y, -z), 0.0F, 1.0F, 0.0F, 0.5F);

            for(int i = 0; i < path.getNodeCount(); ++i) {
                Node node = path.getNode(i);
                if (distanceToCamera(node.asBlockPos(), x, y, z) <= 80.0F) {
                    float f = i == path.getNextNodeIndex() ? 1.0F : 0.0F;
                    float f1 = i == path.getNextNodeIndex() ? 0.0F : 1.0F;
                    DebugRenderer.renderFilledBox(poseStack, buffer, (new AABB((double)((float)node.x + 0.5F - p_270841_), (double)((float)node.y + 0.01F * (float)i), (double)((float)node.z + 0.5F - p_270841_), (double)((float)node.x + 0.5F + p_270841_), (double)((float)node.y + 0.25F + 0.01F * (float)i), (double)((float)node.z + 0.5F + p_270841_))).move(-x, -y, -z), f, 0.0F, f1, 0.5F);
                }
            }
        }

        Path.DebugData path$debugdata = path.debugData();
        if (p_270481_ && path$debugdata != null) {
            for(Node node1 : path$debugdata.closedSet()) {
                if (distanceToCamera(node1.asBlockPos(), x, y, z) <= 80.0F) {
                    DebugRenderer.renderFilledBox(poseStack, buffer, (new AABB((double)((float)node1.x + 0.5F - p_270841_ / 2.0F), (double)((float)node1.y + 0.01F), (double)((float)node1.z + 0.5F - p_270841_ / 2.0F), (double)((float)node1.x + 0.5F + p_270841_ / 2.0F), (double)node1.y + 0.1, (double)((float)node1.z + 0.5F + p_270841_ / 2.0F))).move(-x, -y, -z), 1.0F, 0.8F, 0.8F, 0.5F);
                }
            }

            for(Node node3 : path$debugdata.openSet()) {
                if (distanceToCamera(node3.asBlockPos(), x, y, z) <= 80.0F) {
                    DebugRenderer.renderFilledBox(poseStack, buffer, (new AABB((double)((float)node3.x + 0.5F - p_270841_ / 2.0F), (double)((float)node3.y + 0.01F), (double)((float)node3.z + 0.5F - p_270841_ / 2.0F), (double)((float)node3.x + 0.5F + p_270841_ / 2.0F), (double)node3.y + 0.1, (double)((float)node3.z + 0.5F + p_270841_ / 2.0F))).move(-x, -y, -z), 0.8F, 1.0F, 1.0F, 0.5F);
                }
            }
        }

        if (p_270748_) {
            for(int j = 0; j < path.getNodeCount(); ++j) {
                Node node2 = path.getNode(j);
                if (distanceToCamera(node2.asBlockPos(), x, y, z) <= 80.0F) {
                    DebugRenderer.renderFloatingText(poseStack, buffer, String.valueOf(node2.type), (double)node2.x + (double)0.5F, (double)node2.y + (double)0.75F, (double)node2.z + (double)0.5F, -1, 0.02F, true, 0.0F, true);
                    DebugRenderer.renderFloatingText(poseStack, buffer, String.format(Locale.ROOT, "%.2f", node2.costMalus), (double)node2.x + (double)0.5F, (double)node2.y + (double)0.25F, (double)node2.z + (double)0.5F, -1, 0.02F, true, 0.0F, true);
                }
            }
        }

    }
}
