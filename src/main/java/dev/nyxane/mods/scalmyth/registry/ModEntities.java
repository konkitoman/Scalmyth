package dev.nyxane.mods.scalmyth.registry;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.entity.CrowEntity;
import dev.nyxane.mods.scalmyth.entity.ScalmythEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ScalmythAPI.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<ScalmythEntity>>
        SCALMYTH = ENTITY_TYPES.register("scalmyth", () ->
        EntityType.Builder.of(ScalmythEntity::new,
                MobCategory.MONSTER)
            .sized(8.0f, 3.0f)
            .spawnDimensionsScale(8f)
            .setTrackingRange(10)
            .clientTrackingRange(10)
            .eyeHeight(2)
            .build(ScalmythAPI.rl("scalmyth").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<CrowEntity>>
        CROW = ENTITY_TYPES.register("crow", () ->
        EntityType.Builder.of(CrowEntity::new,
                MobCategory.CREATURE)
            .sized(0.25f, 0.5f)
            .spawnDimensionsScale(1f)
            .setTrackingRange(10)
            .clientTrackingRange(10)
            .eyeHeight(0.45F)
            .build(ScalmythAPI.rl("crow").toString()));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @EventBusSubscriber(modid = ScalmythAPI.MOD_ID)
    public static class ModEvents {
        @SubscribeEvent
        public static void entityAttributeEvent(final EntityAttributeCreationEvent event) {
            event.put(ModEntities.SCALMYTH.get(), ScalmythEntity.setAttributes());
            event.put(ModEntities.CROW.get(), CrowEntity.setAttributes());
        }
    }
}


