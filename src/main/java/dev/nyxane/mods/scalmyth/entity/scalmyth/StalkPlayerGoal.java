package dev.nyxane.mods.scalmyth.entity.scalmyth;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class StalkPlayerGoal extends Goal {
    private final ScalmythEntity mob;
    private LivingEntity target;
    private final double speed;
    private final double maxDistance;

    public StalkPlayerGoal(ScalmythEntity mob, double speed, double maxDistance) {
        this.mob = mob;
        this.speed = speed;
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean canUse() {
        target = mob.level().getNearestPlayer(mob, maxDistance);
        return target != null && !mob.getSensing().hasLineOfSight(target);
    }

    @Override
    public void tick() {
        if (target == null) return;
        Vec3 direction = target.position().subtract(mob.position()).normalize();
        Vec3 stalkPos = mob.position().add(direction.scale(0.5 + mob.getRandom().nextDouble()));
        mob.getNavigation().moveTo(stalkPos.x, stalkPos.y, stalkPos.z, speed);
    }
}
