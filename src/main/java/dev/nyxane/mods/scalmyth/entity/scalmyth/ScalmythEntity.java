package dev.nyxane.mods.scalmyth.entity.scalmyth;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.Sounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
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
  private Level tempWorld; //reusable value for world, declared here to save ram access costs

  private int stage = 0;
  private int stage_tick = 0;

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
    AttributeSupplier.Builder builder = Mob.createMobAttributes();
    builder = builder.add(Attributes.MOVEMENT_SPEED, 0.3);
    builder = builder.add(Attributes.MAX_HEALTH, 200);
    builder = builder.add(Attributes.ARMOR, 0);
    builder = builder.add(Attributes.ATTACK_DAMAGE, 4);
    builder = builder.add(Attributes.FOLLOW_RANGE, 128);
    builder = builder.add(Attributes.STEP_HEIGHT, 2);
    return builder.build();
  }

  @Override
  protected void registerGoals() {
    super.registerGoals();

    AvoidEntityGoal avoid_player = new AvoidEntityGoal(this, Player.class, 50F, 3D, 1.0D){
      @Override
      public boolean canUse() {
        if (stage != 1) return false;

        return super.canUse();
      }

      @Override
      public boolean canContinueToUse() {
        if (stage != 1) return false;

        if (!this.pathNav.isDone()) return true;

        if (toAvoid != null && distanceTo(toAvoid) > 40){
          stage = 2;
          this.stop();
        }

        return false;
      }
    };

    this.goalSelector.addGoal(1, new BreathAirGoal(this) {
      @Override
      public boolean canUse() {
        tempWorld = ScalmythEntity.this.level(); //is run actively, unlike addGoal, so this is needed to be set constantly
        return super.canUse() && ScalmythEntity.this.isInWater();
      }
      @Override
      public boolean canContinueToUse() {
        tempWorld = ScalmythEntity.this.level(); //is run actively, unlike addGoal, so this is needed to be set constantly
        return super.canContinueToUse() && ScalmythEntity.this.isInWater();
      }
    });
    this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true) {
      @Override
      public boolean canUse() {
        if (stage == 0 || stage == 2){
          return super.canUse();
        }
        return false;
      }

      @Override
      public boolean canContinueToUse() {
        if (stage == 0 || stage == 2){
          return super.canContinueToUse();
        }
        return false;
      }

      @Override
      protected boolean canPerformAttack(LivingEntity entity) {
        switch (stage){
          case 0:
            ScalmythAPI.LOGGER.info("Distance: {}", this.mob.distanceToSqr(entity));
            if (this.mob.distanceTo(entity) < 11 ){
              if (stage_tick > 50){
                ScalmythAPI.LOGGER.info("RUN");
                stage = 1;
                this.stop();
              }

              stage_tick += 1;
            }
            break;
          case 1: return false;
          case 2:
            boolean res = this.isTimeToAttack() && this.mob.distanceTo(entity) < 11 && this.mob.getSensing().hasLineOfSight(entity);

/*            if (res){
              stage = 0;
              stage_tick = 0;
            }*/

            return res;
        }

        return false;
      }
    });
    targetSelector.addGoal(1, avoid_player);
    this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, false, false));
    this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, ServerPlayer.class, false, false));
    this.targetSelector.addGoal(5, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(6, new NearestAttackableTargetGoal(this, Villager.class, false, false));
    this.goalSelector.addGoal(7, new BreakDoorGoal(this, e -> true));
    this.goalSelector.addGoal(8, new RemoveBlockGoal(Blocks.GLASS, this, 1, (int) 3));
    this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1));
    this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    this.goalSelector.addGoal(11, new FloatGoal(this));
  }

  @Override
  public boolean hurt(DamageSource damagesource, float amount) { //commented out for now, to make it impossible to damage- in most cases
      if (damagesource.is(DamageTypes.GENERIC_KILL) || damagesource.is(DamageTypes.IN_WALL)){
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
    return Sounds.SCALMYTH_DEATH.get();
  }

  @Override
  protected void playAttackSound() {
    this.playSound(Sounds.SCALMYTH_ATTACK.get());
  }

  @Override
  protected void playStepSound(BlockPos pos, BlockState state) {
    this.playSound(Sounds.SCALMYTH_FOOTSTEPS.get());
  }
}
