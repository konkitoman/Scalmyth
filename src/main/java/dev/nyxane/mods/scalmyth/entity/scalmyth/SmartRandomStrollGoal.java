package dev.nyxane.mods.scalmyth.entity.scalmyth;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SmartRandomStrollGoal extends Goal {
    private final ScalmythEntity mob;
    private final double speed;
    private final int maxDistance;

    public SmartRandomStrollGoal(ScalmythEntity mob, double speed, int maxDistance) {
        this.mob = mob;
        this.speed = speed;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean canUse() {
        return mob.getNavigation().isDone();
    }

    @Override
    public void tick() {
        Vec3 randomOffset = new Vec3(
                (mob.getRandom().nextDouble() - 0.5) * maxDistance,
                (mob.getRandom().nextDouble() - 0.5) * maxDistance,
                (mob.getRandom().nextDouble() - 0.5) * maxDistance
        );
        Vec3 targetPos = mob.position().add(randomOffset);

        // Avoid LOS to nearest player if possible
        Player nearest = mob.level().getNearestPlayer(mob, maxDistance);
        if (nearest != null && mob.getSensing().hasLineOfSight(nearest)) {
            targetPos = targetPos.add(new Vec3(0, 0, 2)); // move slightly away
        }

        mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
    }
}
