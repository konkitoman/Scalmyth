package dev.nyxane.mods.scalmyth.blocks;

import com.mojang.serialization.MapCodec;
import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import dev.nyxane.mods.scalmyth.client.MeshBlockRenderer;
import dev.nyxane.mods.scalmyth.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MeshBlock extends BaseEntityBlock {
    public static final MapCodec<MeshBlock> CODEC = simpleCodec(MeshBlock::new);
    public MeshBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MeshBlockEntity(blockPos, blockState);
    }

    private VoxelShape shape = null;

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (shape != null) return shape;

        var o_meshBlockEntity = level.getBlockEntity(pos, ModBlocks.MESH_ENTITY.get());

        if (o_meshBlockEntity.isEmpty()) return Shapes.block();

        var meshBlockEntity = o_meshBlockEntity.get();
        var mc = Minecraft.getInstance();
        var resource_manager = mc.getResourceManager();
        var resource = resource_manager.getResource(meshBlockEntity.model_location);
        if (resource.isPresent()) {
            try {
                var file = resource.get().open();
                var text = new String(file.readAllBytes());
                ScalmythAPI.LOGGER.info("Model was found");
                var model = new MeshBlockRenderer.BlenderOBJ(text);
                ScalmythAPI.LOGGER.info("Loaded Model: {}", meshBlockEntity.model_location);
                if (!model.isSuccess()) {
                    return Shapes.block();
                }

                ScalmythAPI.LOGGER.info("Collision model loaded!");

                shape = model.buildCollision();
                return shape;
            } catch (Exception e) {
                ScalmythAPI.LOGGER.error(e.toString());
                return Shapes.block();
            }
        }

        return Shapes.block();
    }
}
