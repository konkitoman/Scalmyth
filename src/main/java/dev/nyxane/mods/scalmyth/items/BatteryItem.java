package dev.nyxane.mods.scalmyth.items;

import dev.nyxane.mods.scalmyth.registry.ModComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Supplier;

public class BatteryItem extends Item {

    public static final int MAX_ENERGY = 1000;

    public BatteryItem(Properties properties) {
        super(properties);
    }

    private static DataComponentType<Integer> type() {
        return ModComponents.BATTERY_ENERGY.get();
    }

    public static int getEnergy(ItemStack stack) {
        Integer energy = stack.get(type());
        // If battery doesn't contain component, default to MAX_ENERGY (full battery)
        return energy == null ? MAX_ENERGY : energy;
    }

    public static void setEnergy(ItemStack stack, int energy) {
        energy = Math.max(0, Math.min(energy, MAX_ENERGY));
        stack.set(type(), energy);
    }

    public static void removeEnergy(ItemStack stack) {
        stack.remove(type());
    }
}
