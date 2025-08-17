package dev.nyxane.mods.scalmyth.mixin;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
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
      if (entity instanceof ScalmythEntity){
          Node node = (Node)this.nodes.get(index);
          double d0 = (double)node.x + 0.5;
          double d1 = (double)node.y;
          double d2 = (double)node.z + 0.5;
          return new Vec3(d0, d1, d2);
      }else{
          Node node = (Node)this.nodes.get(index);
          double d0 = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0F)) * (double)0.5F;
          double d1 = (double)node.y;
          double d2 = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0F)) * (double)0.5F;
          return new Vec3(d0, d1, d2);
      }
  }
}
