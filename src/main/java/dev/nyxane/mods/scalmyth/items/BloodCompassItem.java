package dev.nyxane.mods.scalmyth.items;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.ModDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class BloodCompassItem extends CompassItem {
    // TODO config
    // TODO display state (in tooltip)
    public static final int SEARCH_RADIUS = 10000;
    public static final int SEARCH_HORIZONTAL_STEP = 5;
    public static final int SEARCH_VERTICAL_STEP = 5;

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // prevents using lodestone to overwrite the target.
        // this is identical to Item#useOn, skipping the CompassItem impl
        return InteractionResult.PASS;
    }

    public BloodCompassItem() {
        this(new Properties().stacksTo(1));
    }

    public BloodCompassItem(Properties properties) {
        super(properties);
    }

    private static void setTarget(ItemStack stack, GlobalPos target) {
        stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(target), true));
    }

    private Optional<GlobalPos> getTarget(ItemStack stack) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if(tracker == null)
            return Optional.empty();
        return tracker.target();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.scalmyth.blood_compass";
    }

    private void locateAndSetTarget(ItemStack stack, Level level, BlockPos start) {
        ScalmythAPI.LOGGER.info("Searching for biome in dimension {}", level.dimension());
        stack.set(ModDataComponents.BLOOD_COMPASS_STATE, State.SEARCHING);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Searching..."))));

        // TODO location (requires modifying biome gen so it's O(1).
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if(getTarget(stack).isEmpty() && !level.isClientSide &&
                stack.get(ModDataComponents.BLOOD_COMPASS_STATE) != State.SEARCHING) {
            locateAndSetTarget(stack, level, BlockPos.containing(player.getPosition(0)));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    public enum State {
        UNLOCATED,
        SEARCHING,
        LOCATED,
        FAILURE
    }

    public static class StateCodec implements StreamCodec<ByteBuf, State> {
        @Override
        public State decode(ByteBuf byteBuf) {
            byte b = byteBuf.readByte();
            return switch(b) {
                case 0 -> State.UNLOCATED;
                case 1 -> State.SEARCHING;
                case 2 -> State.LOCATED;
                case 3 -> State.FAILURE;
                default -> throw new IllegalStateException("Unexpected value: " + b);
            };
        }

        @Override
        public void encode(ByteBuf o, @NotNull State state) {
            o.writeByte(state.ordinal());
        }
    }
}
