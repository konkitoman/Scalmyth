package dev.nyxane.mods.scalmyth.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import dev.nyxane.mods.scalmyth.KDebug;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import dev.nyxane.mods.scalmyth.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ScalmythEntity extends Monster implements GeoEntity, VibrationSystem {
    protected static final RawAnimation IDLE_1 = RawAnimation.begin().then("idle_1", Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_2 = RawAnimation.begin().then("idle_2", Animation.LoopType.LOOP);
    protected static final RawAnimation IDLE_3 = RawAnimation.begin().then("idle_3", Animation.LoopType.LOOP);
    protected static final RawAnimation WALK = RawAnimation.begin().then("walk", Animation.LoopType.LOOP);
    protected static final RawAnimation WALKING = RawAnimation.begin().then("walking", Animation.LoopType.LOOP);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final DynamicGameEventListener<Listener> dynamicGameEventListener = new DynamicGameEventListener(new VibrationSystem.Listener(this));
    private final VibrationSystem.User vibrationUser = new ScalmythEntity.VibrationUser();
    private final VibrationSystem.Data vibrationData = new VibrationSystem.Data();

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
    protected Brain<ScalmythEntity> makeBrain(Dynamic<?> dynamic) {
        return Ai.makeBrain(this, dynamic);
    }

    @Override
    public Brain<ScalmythEntity> getBrain() {
        return (Brain<ScalmythEntity>) super.getBrain();
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

    private static BlockHitResult _entityLineOfSightWith(LivingEntity entity, Vec3 offset, Vec3 target) {
        Level level = entity.level();
        BlockHitResult hit = level.clip(new ClipContext(entity.position().add(offset), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (hit.getType() == HitResult.Type.MISS) {
            KDebug.addShape(level, new KDebug.Shape.Lines(entity.position().add(offset), target, 0xff00ff00)
                .setId(List.of(10, entity, offset, target)));
        } else {
            KDebug.addShape(level, new KDebug.Shape.Lines(entity.position().add(offset), target, 0xffff0000)
                .setId(List.of(10, entity, offset, target)));
        }

        return hit;
    }

    private static boolean entityLineOfSightWith(LivingEntity entity, Vec3 target) {
        BlockHitResult hit = _entityLineOfSightWith(entity, Vec3.ZERO, target);

        for (int y = 0; y <= entity.getBbHeight(); y++) {
            for (int i = -(int) (entity.getBbWidth() / 2); i < entity.getBbWidth() / 2; i++) {
                if (hit.getType() == HitResult.Type.BLOCK) break;
                hit = _entityLineOfSightWith(entity, new Vec3(i, y, 0), target.add(i, y, 0));
                if (hit.getType() == HitResult.Type.BLOCK) break;
                hit = _entityLineOfSightWith(entity, new Vec3(0, y, i), target.add(0, y, i));
            }
        }

        return hit.getType() == HitResult.Type.MISS;
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> listenerConsumer) {
        super.updateDynamicGameEventListener(listenerConsumer);
        if (level() instanceof ServerLevel serverLevel) {
            listenerConsumer.accept(dynamicGameEventListener, serverLevel);
        }
    }

    @Override
    public void tick() {
        super.tick();
        var level = level();

        if (level instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, vibrationData, vibrationUser);
            getBrain().tick(serverLevel, this);
        }
    }

    @Override
    public Data getVibrationData() {
        return vibrationData;
    }

    @Override
    public User getVibrationUser() {
        return vibrationUser;
    }

    class VibrationUser implements VibrationSystem.User {
        private final PositionSource positionSource = new EntityPositionSource(ScalmythEntity.this, ScalmythEntity.this.getEyeHeight());

        public int getListenerRadius() {
            return 16;
        }

        public @NotNull PositionSource getPositionSource() {
            return this.positionSource;
        }

        public @NotNull TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.VIBRATIONS;
        }

        public boolean canTriggerAvoidVibration() {
            return true;
        }

        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event, GameEvent.Context c) {
            ScalmythAPI.LOGGER.info("Can receive vibration from: {}, event: {}, context: {}", pos, event, c);

            return true;
        }

        public void onReceiveVibration(ServerLevel level, BlockPos pos, Holder<GameEvent> event, @Nullable Entity e1, @Nullable Entity e2, float f) {
            ScalmythAPI.LOGGER.info("Vibration from: {}, event: {}, e1: {}, e2: {}, f: {}", pos, event, e1, e2, f);
        }
    }

    public static class Ai {
        public static final SensorType<NearestCorruptedBlock> NEAREST_CORRUPTED_BLOCK = new SensorType<NearestCorruptedBlock>(NearestCorruptedBlock::new);
        public static final MemoryModuleType<ArrayList<BlockPos>> CURE_BLOCKS = new MemoryModuleType<>(Optional.empty());

        public static class NearestCorruptedBlock extends Sensor<ScalmythEntity> {

            @Override
            protected void doTick(ServerLevel serverLevel, ScalmythEntity scalmythEntity) {
                if (scalmythEntity.getBrain().getMemory(CURE_BLOCKS).isPresent()) return;

                var blocks_to_cure = findNearestBlock(scalmythEntity, s -> s.is(ModBlocks.ASHEN_GRASS), 21);

                if (!blocks_to_cure.isEmpty()) {
                    scalmythEntity.getBrain().setMemory(CURE_BLOCKS, blocks_to_cure);
                }
            }

            @Override
            public Set<MemoryModuleType<?>> requires() {
                return Set.of();
            }

            private static ArrayList<BlockPos> findNearestBlock(LivingEntity entity, Predicate<BlockState> predicate, double distance) {
                var blocks = new ArrayList<BlockPos>();
                BlockPos blockpos = entity.blockPosition();
                BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                for (int i = -1; (double) i <= 0; i += 1) {
                    for (int j = 0; (double) j < distance; ++j) {
                        for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                            for (int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                                blockpos$mutableblockpos.setWithOffset(blockpos, k, i - 1, l);
                                if (blockpos.closerThan(blockpos$mutableblockpos, distance)) {
                                    if (predicate.test(entity.level().getBlockState(blockpos$mutableblockpos))) {
                                        KDebug.addShape(entity.level(), new KDebug.Shape.Box(blockpos$mutableblockpos.immutable()).setColor(0x2000ff00));
                                        blocks.add(blockpos$mutableblockpos.immutable());
                                    } else {
                                        KDebug.addShape(entity.level(), new KDebug.Shape.Box(blockpos$mutableblockpos.immutable()).setColor(0x20ff0000));
                                    }
                                }
                            }
                        }
                    }
                }

                Collections.reverse(blocks);

                return blocks;
            }
        }

        private static class Cure extends Behavior<ScalmythEntity> {
            Path path;

            public Cure() {
                super(Map.of(CURE_BLOCKS, MemoryStatus.VALUE_PRESENT));
            }

            @Override
            protected boolean canStillUse(ServerLevel level, ScalmythEntity entity, long gameTime) {
                return entity.getBrain().getMemory(CURE_BLOCKS).isPresent();
            }

            @Override
            protected void tick(ServerLevel level, ScalmythEntity owner, long gameTime) {
                var to_cure = owner.getBrain().getMemory(CURE_BLOCKS).get();

                to_cure.forEach(blockPos -> KDebug.addShape(level, new KDebug.Shape.Box(blockPos).setColor(0xaa00ff00).setId(blockPos)));

                if (to_cure.isEmpty()) {
                    owner.getBrain().setMemory(CURE_BLOCKS, Optional.empty());
                    return;
                }

                if (to_cure.getLast().distManhattan(owner.blockPosition()) <= 5) {
                    level.setBlockAndUpdate(to_cure.getLast(), Blocks.GRASS_BLOCK.defaultBlockState());
                    to_cure.removeLast();
                    path = null;
                } else {
                    if (path == null) {
                        path = owner.getNavigation().createPath(to_cure.getLast(), 0);
                    }
                    owner.getNavigation().moveTo(path, 1);
                }
            }

            @Override
            protected void stop(ServerLevel level, ScalmythEntity entity, long gameTime) {
                path = null;
                entity.getNavigation().stop();
            }
        }

        private static final List<SensorType<? extends Sensor<? super ScalmythEntity>>> SENSOR_TYPES = List.of(NEAREST_CORRUPTED_BLOCK);
        private static final List<MemoryModuleType<?>> MEMORY_TYPES = List.of(CURE_BLOCKS);

        protected static Brain<ScalmythEntity> makeBrain(ScalmythEntity scalmyth, Dynamic<?> ops) {
            Brain.Provider<ScalmythEntity> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
            Brain<ScalmythEntity> brain = provider.makeBrain(ops);
            brain.addActivity(Activity.CORE, 0, ImmutableList.of(new Cure()));
            brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
            brain.setDefaultActivity(Activity.IDLE);
            brain.useDefaultActivity();

            return brain;
        }
    }
}
