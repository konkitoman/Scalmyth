package dev.nyxane.mods.scalmyth.entity.scalmyth;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

public class SmartTargetGoal<T extends LivingEntity> extends TargetGoal {
    private final Class<T> targetClass;
    private final boolean checkSight;
    private T target;

    public SmartTargetGoal(Mob mob, Class<T> targetClass, boolean checkSight) {
        super(mob, checkSight);
        this.targetClass = targetClass;
        this.checkSight = checkSight;
    }

    @Override
    public boolean canUse() {
        // Find nearest entity of targetClass within 16 blocks
        target = mob.level().getEntitiesOfClass(targetClass, mob.getBoundingBox().inflate(16),
                        e -> e.isAlive() && (mob.distanceToSqr(e) < 256)) // 16 blocks squared
                .stream()
                .filter(e -> !checkSight || mob.getSensing().hasLineOfSight(e))
                .min((a, b) -> Double.compare(mob.distanceToSqr(a), mob.distanceToSqr(b)))
                .orElse(null);

        return target != null;
    }

    @Override
    public void start() {
        mob.setTarget(target);
        super.start();
    }

    @Override
    public void stop() {
        mob.setTarget(null);
        super.stop();
    }
}
