package dev.nyxane.mods.scalmyth.entity.scalmyth;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class AmbushAttackGoal extends Goal {
    private final ScalmythEntity mob;
    private LivingEntity target;
    private final double speed;

    public AmbushAttackGoal(ScalmythEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return target != null && mob.getSensing().hasLineOfSight(target) && mob.distanceTo(target) < 10;
    }

    @Override
    public void tick() {
        if (target == null) return;
        mob.getNavigation().moveTo(target, speed);
        if (mob.distanceTo(target) < 2) {
            mob.doHurtTarget(target);
        }
    }
}
