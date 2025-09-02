package dev.nyxane.mods.scalmyth.items;

import dev.nyxane.mods.scalmyth.registry.ModComponents;
import dev.nyxane.mods.scalmyth.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class FlashlightItem extends Item {

    private final FlashlightRenderer flashlightRenderer;
    private static final int ENERGY_PER_TICK = 1;

    public FlashlightItem(Properties properties) {
        super(properties);
        this.flashlightRenderer = new FlashlightRenderer();
    }

    // ---- Use flow: insert/remove/toggle ----
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();
        boolean sneaking = player.isShiftKeyDown();

        // 1) Remove battery (sneak + use) - do server-side removal & give battery item back
        if (sneaking && hasBattery(stack)) {
            if (!level.isClientSide) {
                int energy = getBatteryEnergy(stack);
                ItemStack removed = new ItemStack(ModItems.BATTERY.get());
                BatteryItem.setEnergy(removed, energy);
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }
                removeBattery(stack);
                setFlashlightOn(stack, false);
            }
            // sidedSuccess ensures the client performs hand-swing locally
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        // 2) Insert battery from offhand (press use with flashlight in main hand, battery in offhand)
        // use equality against the registered battery item to avoid class mismatches
        if (!sneaking && !offhand.isEmpty() && offhand.getItem() == ModItems.BATTERY.get()) {
            if (!level.isClientSide) {
                if (!hasBattery(stack)) {
                    insertBattery(stack, offhand);
                    if (!player.getAbilities().instabuild) {
                        offhand.shrink(1);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        // 3) Toggle flashlight: **only toggle server-side** (server is authoritative); client renderer will follow via network sync
        if (!level.isClientSide) {
            if (hasBattery(stack) && getBatteryEnergy(stack) > 0) {
                boolean now = !isFlashlightOn(stack);
                setFlashlightOn(stack, now);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // ---- inventoryTick: drain server-side, render client-side ----
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        // server: drain energy when selected and flashlight is on
        if (!level.isClientSide && selected && isFlashlightOn(stack) && hasBattery(stack)) {
            int energy = getBatteryEnergy(stack) - ENERGY_PER_TICK;
            BatteryItem.setEnergy(stack, energy);
            if (energy <= 0) {
                // battery dead: remove component and turn off flashlight
                removeBattery(stack);
                setFlashlightOn(stack, false);
            }
        }

        // client: update renderer while selected and flashlight is on
        if (level.isClientSide) {
            boolean shouldBeOn = selected && isFlashlightOn(stack) && hasBattery(stack) && getBatteryEnergy(stack) > 0;
            if (shouldBeOn) {
                // ensure renderer is on and update position/brightness
                if (!flashlightRenderer.isOn()) flashlightRenderer.toggleOn();
                flashlightRenderer.updateFromHead(Minecraft.getInstance(), Minecraft.getInstance().getFrameTimeNs());

                int energy = getBatteryEnergy(stack);
                float ratio = Math.max(0f, energy / (float) BatteryItem.MAX_ENERGY);

                if (energy < BatteryItem.MAX_ENERGY * 0.2f && Math.random() < 0.1) {
                    flashlightRenderer.setBrightness(ratio * 1.5f * 0.3f); // flicker
                } else {
                    flashlightRenderer.setBrightness(Math.max(0.1f, ratio * 1.5f));
                }
            } else {
                if (flashlightRenderer.isOn()) flashlightRenderer.toggleOff();
            }
        }
    }

    // ---- Component helpers ----
    private static DataComponentType<Integer> batteryType() {
        return ModComponents.BATTERY_ENERGY.get();
    }
    private static DataComponentType<Boolean> onType() {
        return ModComponents.FLASHLIGHT_ON.get();
    }

    private boolean hasBattery(ItemStack stack) {
        return stack.get(batteryType()) != null;
    }

    private int getBatteryEnergy(ItemStack stack) {
        Integer v = stack.get(batteryType());
        return v == null ? 0 : v;
    }

    private void setBatteryEnergy(ItemStack stack, int energy) {
        BatteryItem.setEnergy(stack, energy);
    }

    private void insertBattery(ItemStack flashlight, ItemStack battery) {
        setBatteryEnergy(flashlight, BatteryItem.getEnergy(battery));
    }

    private void removeBattery(ItemStack flashlight) {
        flashlight.remove(batteryType());
    }

    private boolean isFlashlightOn(ItemStack stack) {
        Boolean v = stack.get(onType());
        return v != null && v;
    }

    private void setFlashlightOn(ItemStack stack, boolean on) {
        if (on) stack.set(onType(), true);
        else stack.remove(onType());
    }
}
