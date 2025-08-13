package dev.nyxane.mods.scalmyth.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class BloodFlowerBlock extends BushBlock implements SuspiciousEffectHolder {
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getBlock() instanceof AshenGrassBlock){
            return true;
        }
        return false;
    }
    protected static final MapCodec<SuspiciousStewEffects> EFFECTS_FIELD;
    public static final MapCodec<FlowerBlock> CODEC;
    protected static final float AABB_OFFSET = 3.0F;
    protected static final VoxelShape SHAPE;
    private final SuspiciousStewEffects suspiciousStewEffects;

    public MapCodec<? extends FlowerBlock> codec() {
        return CODEC;
    }

    public BloodFlowerBlock(Holder<MobEffect> effect, float seconds, BlockBehaviour.Properties properties) {
        this(makeEffectList(effect, seconds), properties);
    }

    public BloodFlowerBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
        super(properties);
        this.suspiciousStewEffects = suspiciousStewEffects;
    }

    protected static SuspiciousStewEffects makeEffectList(Holder<MobEffect> effect, float seconds) {
        return new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(effect, Mth.floor(seconds * 20.0F))));
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 vec3 = state.getOffset(level, pos);
        return SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    public SuspiciousStewEffects getSuspiciousEffects() {
        return this.suspiciousStewEffects;
    }

    static {
        EFFECTS_FIELD = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
        CODEC = RecordCodecBuilder.mapCodec((p_308824_) -> p_308824_.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(p_308824_, FlowerBlock::new));
        SHAPE = Block.box((double)5.0F, (double)0.0F, (double)5.0F, (double)11.0F, (double)10.0F, (double)11.0F);
    }
}
