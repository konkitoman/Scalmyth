package dev.nyxane.mods.scalmyth.entity.scalmyth;

import dev.nyxane.mods.scalmyth.entity.scalmyth.ScalmythEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PredatorHuntGoal extends Goal {
    private final ScalmythEntity predator;
    private LivingEntity target;
    private Vec3 lastKnownPosition;
    private int searchCooldown;

    public PredatorHuntGoal(ScalmythEntity predator) {
        this.predator = predator;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.target = predator.getTarget();
        if (target == null) return false;
        lastKnownPosition = target.position();
        return !predator.getSensing().hasLineOfSight(target);
    }

    @Override
    public void start() {
        searchCooldown = 0;
    }

    @Override
    public void tick() {
        if (lastKnownPosition != null) {
            predator.getNavigation().moveTo(lastKnownPosition.x, lastKnownPosition.y, lastKnownPosition.z, 1.2);
        }
        searchCooldown++;
        if (searchCooldown > 200) {

            stop();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return searchCooldown <= 200;
    }
}
