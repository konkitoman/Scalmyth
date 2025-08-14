package dev.nyxane.mods.scalmyth.worldgen;


import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.world.level.block.grower.TreeGrower;

import java.util.Optional;

public class ModTreeGrowers {
    public static final TreeGrower ASHEN_TREE = new TreeGrower(ScalmythAPI.MOD_ID + ":ashen_tree",
            Optional.empty(), Optional.of(ModConfiguredFeatures.ASHEN_TREE), Optional.empty()); // TODO megatree
}
