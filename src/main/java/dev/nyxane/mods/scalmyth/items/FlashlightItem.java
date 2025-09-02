package dev.nyxane.mods.scalmyth.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlashlightItem extends Item {

    private final FlashlightRenderer flashlightRenderer;

    public FlashlightItem(Properties properties) {
        super(properties);
        flashlightRenderer = new FlashlightRenderer();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            flashlightRenderer.toggle();
            flashlightRenderer.updateFromHead(
                    Minecraft.getInstance(),
                    Minecraft.getInstance().getFrameTimeNs()
            );
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide) {
            if (selected) {
                if (flashlightRenderer.isOn()) {
                    flashlightRenderer.updateFromHead(
                            Minecraft.getInstance(),
                            Minecraft.getInstance().getFrameTimeNs()
                    );
                }
            } else {
                if (flashlightRenderer.isOn()) {
                    flashlightRenderer.toggle();
                }
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
}
