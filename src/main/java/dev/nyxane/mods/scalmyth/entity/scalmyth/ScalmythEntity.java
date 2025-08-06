package dev.nyxane.mods.scalmyth.entity.scalmyth;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

public class ScalmythEntity extends Monster implements GeoEntity {
    private static final Random RANDOM = new Random();
//    protected static final RawAnimation STATIC_POSE = RawAnimation.begin().then("static_pose", Animation.LoopType.LOOP);
//    protected static final RawAnimation STATIC_POSE_2;
//    protected static final RawAnimation SNEAKER;
    protected static final RawAnimation IDLE_1 = RawAnimation.begin().then("idle_1",Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_2 = RawAnimation.begin().then("idle_2",Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_3 = RawAnimation.begin().then("idle_3",Animation.LoopType.LOOP);
    protected static final RawAnimation WALK = RawAnimation.begin().then("walk",Animation.LoopType.LOOP);
    protected static final RawAnimation WALKING = RawAnimation.begin().then("walking",Animation.LoopType.LOOP);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ScalmythEntity(EntityType<? extends Monster> entityType, Level level) {

        super(entityType, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walkController", 1, this::walkController));
        controllers.add(new AnimationController<>(this, "idleController", 1, this::idleController));

    }

    protected <E extends ScalmythEntity>PlayState walkController(final AnimationState<E> event) {
        if(event.isMoving())
            return event.setAndContinue(WALKING);
        return PlayState.STOP;
    }

    protected <E extends ScalmythEntity>PlayState idleController(final AnimationState<E> event) {
        if (event.isMoving()) {
            return PlayState.STOP;
        }
        if(
            event.getController().getAnimationState() == AnimationController.State.STOPPED
        ) {

            float r = RANDOM.nextFloat();
            if (r < 0.1f) {
                return event.setAndContinue(IDLE_1);
            } else if (r < 0.4f) {
                return event.setAndContinue(IDLE_2);
            } else {
                return event.setAndContinue(IDLE_3);
            }
        }
        return PlayState.CONTINUE;


    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new RandomStrollGoal(this,0.5f,50));
    }
}
