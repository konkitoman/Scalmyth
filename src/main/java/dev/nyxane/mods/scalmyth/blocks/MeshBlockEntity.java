package dev.nyxane.mods.scalmyth.blocks;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MeshBlockEntity extends BlockEntity {
    public ResourceLocation model_location = ScalmythAPI.rl("model.obj");
    public ResourceLocation texture = ScalmythAPI.rl("textures/block/ashen_stone_brick.png");
    public ResourceLocation light_texture = ScalmythAPI.rl("white.png");

    public MeshBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlocks.MESH_ENTITY.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("model_location", model_location.toString());
        tag.putString("texture", texture.toString());
        tag.putString("light_texture", light_texture.toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        model_location = ResourceLocation.parse(tag.getString("model_location"));
        texture = ResourceLocation.parse(tag.getString("texture"));
        light_texture = ResourceLocation.parse(tag.getString("light_texture"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }
}
