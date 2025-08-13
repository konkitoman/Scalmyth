package dev.nyxane.mods.scalmyth.api;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ScalmythAPI {
    public static final String MOD_ID = "scalmyth";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static ResourceLocation rl(String path) {
      return ResourceLocation.fromNamespaceAndPath(MOD_ID,path);
    }
}
