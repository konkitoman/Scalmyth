package dev.nyxane.mods.scalmyth.mixin;

import dev.nyxane.mods.scalmyth.KDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Path.class)
public abstract class PathMixin {
    @Shadow
    public abstract BlockPos getNextNodePos();

    @Inject(method = "getNextEntityPos", at = @At("RETURN"))
    private void getNextEntityPos(Entity entity, CallbackInfoReturnable<Vec3> cir){
        KDebug.addShape(entity.level(), new KDebug.Shape.Box(cir.getReturnValue(), new Vec3(0.5, 0.5, 0.5)).setColor(0xff00ff00).setId(entity));
        KDebug.addShape(entity.level(), new KDebug.Shape.Box(entity.position(), new Vec3(0.5, 0.5, 0.5)).setColor(0xffffffff).setId(entity.hashCode() + 1));
        KDebug.addShape(entity.level(), new KDebug.Shape.Box(getNextNodePos(), new Vec3(0.5, 0.5, 0.5)).setColor(0xff0000ff).setId(entity.hashCode() + 2));
    }
}
