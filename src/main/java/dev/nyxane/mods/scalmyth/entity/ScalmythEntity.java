package dev.nyxane.mods.scalmyth.entity;

import dev.nyxane.mods.scalmyth.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class ScalmythEntity extends Monster implements GeoEntity, SmartBrainOwner<ScalmythEntity> {
    protected static final RawAnimation IDLE_1 = RawAnimation.begin().then("idle_1", Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_2 = RawAnimation.begin().then("idle_2", Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_3 = RawAnimation.begin().then("idle_3", Animation.LoopType.LOOP);
    protected static final RawAnimation WALK = RawAnimation.begin().then("walk", Animation.LoopType.LOOP);
    protected static final RawAnimation WALKING = RawAnimation.begin().then("walking", Animation.LoopType.LOOP);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int saturation = 0;


    public ScalmythEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static void applyDarknessAround(ServerLevel level, Vec3 pos, @Nullable Entity source, int radius) {
        MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.DARKNESS, 260, 0, false, false);
        MobEffectUtil.addEffectToPlayersAround(level, source, pos, (double) radius, mobeffectinstance, 200);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickBrain(this);
        ServerLevel serverlevel = (ServerLevel) this.level();

        if ((this.tickCount + this.getId()) % 120 == 0) {
            applyDarknessAround(serverlevel, this.position(), this, 50);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "walkController", 1, this::walkController));
        controllers.add(new AnimationController<>(this, "idleController", 1, this::idleController));
    }

    protected <E extends ScalmythEntity> PlayState walkController(final AnimationState<E> event) {
        if (event.isMoving())
            return event.setAndContinue(WALKING);
        return PlayState.STOP;
    }

    protected <E extends ScalmythEntity> PlayState idleController(final AnimationState<E> event) {
        if (event.isMoving()) {
            return PlayState.STOP;
        }
        if (
            event.getController().getAnimationState() == AnimationController.State.STOPPED
        ) {

            float r = getRandom().nextFloat();
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
    public void checkDespawn() {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static AttributeSupplier setAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
        builder = builder.add(Attributes.MAX_HEALTH, 200);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 4);
        builder = builder.add(Attributes.FOLLOW_RANGE, 128);
        builder = builder.add(Attributes.STEP_HEIGHT, 2);
        builder = builder.add(Attributes.ENTITY_INTERACTION_RANGE, 12);
        return builder.build();
    }

    @Override
    public boolean hurt(DamageSource damagesource, float amount) { //commented out for now, to make it impossible to damage- in most cases
        if (damagesource.is(DamageTypes.GENERIC_KILL) || damagesource.is(DamageTypes.IN_WALL)) {
            return super.hurt(damagesource, amount);
        }
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SCALMYTH_DEATH.get();
    }

    @Override
    protected void playAttackSound() {
        this.playSound(ModSounds.SCALMYTH_ATTACK.get());
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.SCALMYTH_FOOTSTEPS.get());
    }

    @Override
    protected AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(4);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    public BrainActivityGroup<? extends ScalmythEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
            new LookAtTarget()
                .noTimeout(),
            new MoveToWalkTarget<>()
                .noTimeout()
        );
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean result = super.doHurtTarget(entity);
        if (!entity.isAlive()) {
            if (entity instanceof CrowEntity) {
                saturation += 1;
            }
        }

        return result;
    }

    @Override
    public BrainActivityGroup<? extends ScalmythEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
            new FirstApplicableBehaviour<ScalmythEntity>(
                new TargetOrRetaliate<ScalmythEntity>() {
                    @Override
                    protected boolean checkExtraStartConditions(ServerLevel level, ScalmythEntity entity) {
                        return saturation < 2 && super.checkExtraStartConditions(level, entity);
                    }
                }
                    .attackablePredicate(entity -> entity instanceof CrowEntity)

            ),
            new FollowEntity<ScalmythEntity, LivingEntity>()
                .following(mob -> {
                    NearestVisibleLivingEntities entities = BrainUtils.getMemory(mob.getBrain(), MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
                    assert entities != null;
                    return entities.findClosest(entity -> {
                        if (entity instanceof Player player) {
                            return !player.isCreative();
                        } else {
                            return false;
                        }
                    }).orElse(null);
                }).canTeleportTo((mob, pos, blockState) -> false)
                .stopFollowingWithin(10)
                .noTimeout()
        );
    }

    @Override
    public BrainActivityGroup<? extends ScalmythEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
            new InvalidateAttackTarget<ScalmythEntity>(),
            new SetWalkTargetToAttackTarget<>()
                .closeEnoughDist((e, d) -> 8),
            new AnimatableMeleeAttack<>(1)
        );
    }

    @Override
    public List<? extends ExtendedSensor<? extends ScalmythEntity>> getSensors() {
        return List.of(
            new NearbyLivingEntitySensor<>(),
            new HurtBySensor<>()
        );
    }
}
