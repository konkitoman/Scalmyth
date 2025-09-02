package dev.nyxane.mods.scalmyth.mixin.kdebug;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Shadow
    @Final
    private Sensing sensing;

    @Shadow
    @Final
    public GoalSelector targetSelector;

    @Shadow
    @Final
    public GoalSelector goalSelector;

    @Shadow
    protected PathNavigation navigation;

    @Shadow
    protected abstract void customServerAiStep();

    @Shadow
    protected MoveControl moveControl;

    @Shadow
    protected LookControl lookControl;

    @Shadow
    protected JumpControl jumpControl;

    @Shadow
    protected abstract void sendDebugPackets();

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Overwrite
    protected final void serverAiStep() {
        ++this.noActionTime;
        ProfilerFiller profilerfiller = this.level().getProfiler();
        profilerfiller.push("sensing");
        this.sensing.tick();
        profilerfiller.pop();
        int i = this.tickCount + this.getId();

        if (kdebug$evaluateAI) {
            if (i % 2 != 0 && this.tickCount > 1) {
                profilerfiller.push("targetSelector");
                this.targetSelector.tickRunningGoals(false);
                profilerfiller.pop();
                profilerfiller.push("goalSelector");
                this.goalSelector.tickRunningGoals(false);
                profilerfiller.pop();
            } else {
                profilerfiller.push("targetSelector");
                this.targetSelector.tick();
                profilerfiller.pop();
                profilerfiller.push("goalSelector");
                this.goalSelector.tick();
                profilerfiller.pop();
            }
        }

        profilerfiller.push("navigation");
        this.navigation.tick();
        profilerfiller.pop();
        profilerfiller.push("mob tick");
        if (kdebug$evaluateAI) {
            this.customServerAiStep();
        }
        profilerfiller.pop();
        profilerfiller.push("controls");
        profilerfiller.push("move");
        this.moveControl.tick();
        profilerfiller.popPush("look");
        this.lookControl.tick();
        profilerfiller.popPush("jump");
        this.jumpControl.tick();
        profilerfiller.pop();
        profilerfiller.pop();
        this.sendDebugPackets();
    }

    @Unique
    private boolean kdebug$evaluateAI = true;

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (!kdebug$evaluateAI) {
            compound.putBoolean("EvaluateAI", false);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("EvaluateAI")) {
            kdebug$evaluateAI = compound.getBoolean("EvaluateAI");
        }
    }
}
