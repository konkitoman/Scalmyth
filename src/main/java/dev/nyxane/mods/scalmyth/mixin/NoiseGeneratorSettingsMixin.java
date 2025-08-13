package dev.nyxane.mods.scalmyth.mixin;

import dev.nyxane.mods.scalmyth.registry.ModBiomes;

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.core.Holder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements ModBiomes.ScalmythModNoiseGeneratorSettings {
    @Unique
    private Holder<DimensionType> scalmyth_dimensionTypeReference;

    @WrapMethod(method = "surfaceRule")
    public SurfaceRules.RuleSource surfaceRule(Operation<SurfaceRules.RuleSource> original) {
        SurfaceRules.RuleSource retval = original.call();
        if (this.scalmyth_dimensionTypeReference != null) {
            retval = ModBiomes.adaptSurfaceRule(retval, this.scalmyth_dimensionTypeReference);
        }
        return retval;
    }

    @Override
    public void scalmyth$setScalmythDimensionTypeReference(Holder<DimensionType> dimensionType) {
        this.scalmyth_dimensionTypeReference = dimensionType;
    }
}