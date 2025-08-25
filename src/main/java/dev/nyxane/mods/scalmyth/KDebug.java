package dev.nyxane.mods.scalmyth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.Stream;

public class KDebug {
    private static final ArrayList<Shape> SHAPES = new ArrayList<>();
    private static final StampedLock SHAPES_LOCK = new StampedLock();
    private static Instant LAST_TIME = Instant.now();

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY,
            double camZ) {
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();

        Duration elapsed = Duration.between(LAST_TIME, Instant.now());
        LAST_TIME = Instant.now();
        double delta = ((double)elapsed.getNano()/Math.pow(10.0, 9)) + (double)elapsed.getSeconds();

        long stamp = SHAPES_LOCK.writeLock();
        SHAPES.removeIf(shape -> {
            if (shape instanceof Shape.Lines lines) {
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugLineStrip(1));

                int len = lines.points.size();
                int color = 0xffff0000;

                for (int i = 0; i < len; i++) {
                    Vec3 point = lines.points.get(i);
                    buffer.addVertex(matrix, (float) (point.x - camX), (float) (point.y - camY),
                            (float) (point.z - camZ));
                    if (lines.colors.size() > i) {
                        Integer tmp = lines.colors.get(i);
                        if (tmp != null) {
                            color = tmp;
                        }
                    }
                    buffer.setColor(color);
                }
            } else if (shape instanceof Shape.Box block) {
                drawBox(matrix, bufferSource, block.origin, block.size, block.color, new Vec3(camX, camY, camZ));
            }

            shape.time -= delta;
            return shape.time < 0;
        });
        SHAPES_LOCK.unlockWrite(stamp);

        poseStack.popPose();
    }

    private static void drawBox(Matrix4f matrix, MultiBufferSource bufferSource, Vec3 origin, Vec3 size, int color,
            Vec3 cam) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugFilledBox());

        double sx = size.x / 2;
        double sy = size.y / 2;
        double sz = size.z / 2;

        // TODO: a better way to draw a box/voxel using triangle strip without flods.
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, -sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, -sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));// FOLD
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, -sz - cam.z).toVector3f()));// FOLD
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, sz - cam.z).toVector3f()));// FOLD
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(sx - cam.x, sy - cam.y, sz - cam.z).toVector3f()));// FOLD
        buffer.setColor(color);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(color);
    }

    public static void addShape(Level level, Shape shape) {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                player.connection.send(new KDebugPayload(shape));
            }
            return;
        }

        long stamp = SHAPES_LOCK.writeLock();
        SHAPES.add(shape);
        SHAPES_LOCK.unlockWrite(stamp);
    }

    public static class Shape {
        public static final MapCodec<Shape> CODEC = new MapCodec<Shape>() {
            @Override
            public <T> DataResult<Shape> decode(DynamicOps<T> ops, MapLike<T> input) {
                float time = Codec.FLOAT.decode(ops, input.get("time")).getOrThrow().getFirst();
                byte variant = Codec.BYTE.decode(ops, input.get("variant")).getOrThrow().getFirst();

                switch (variant) {
                    case 0:
                        Shape.Lines lines = Shape.Lines.CODEC.decode(ops, ops.getMap(input.get("inner")).getOrThrow())
                                .getOrThrow();
                        lines.time = time;
                        return DataResult.success(lines);

                    case 1:
                        Shape.Box box = Shape.Box.CODEC.decode(ops, ops.getMap(input.get("inner")).getOrThrow())
                                .getOrThrow();
                        box.time = time;
                        return DataResult.success(box);

                    default:
                        throw new RuntimeException("unknown variant");
                }
            }

            @Override
            public <T> RecordBuilder<T> encode(Shape input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                RecordBuilder<T> builder = ops.mapBuilder();

                builder.add("time", ops.createFloat(input.time));

                if (input instanceof Shape.Lines lines) {
                    builder.add("variant", ops.createByte((byte) 0));
                    builder.add("inner", Shape.Lines.CODEC.encode(lines, ops, builder).build(ops.empty()));
                } else if (input instanceof Shape.Box box) {
                    builder.add("variant", ops.createByte((byte) 1));
                    builder.add("inner", Shape.Box.CODEC.encode(box, ops,
                            builder).build(ops.empty()));

                }

                return builder;
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString("variant"), ops.createString("time"), ops.createString("inner"));
            }

        };

        float time = 1;

        public Shape setTime(float pTimme) {
            time = pTimme;
            return this;
        }

        public static class Lines extends Shape {
            public static final MapCodec<Shape.Lines> CODEC = new MapCodec<Shape.Lines>() {
                @Override
                public <T> DataResult<Lines> decode(DynamicOps<T> ops, MapLike<T> input) {
                    List<Vec3> points = Vec3.CODEC.listOf().decode(ops, input.get("points")).getOrThrow().getFirst();
                    List<Integer> colors = Codec.INT.listOf().decode(ops, input.get("colors")).getOrThrow().getFirst();

                    return DataResult.success(new Lines(points, colors));
                }

                @Override
                public <T> RecordBuilder<T> encode(Lines input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                    RecordBuilder<T> builder = ops.mapBuilder();

                    builder.add("points", Vec3.CODEC.listOf().encode(input.points, ops, ops.empty()));
                    builder.add("colors", Codec.INT.listOf().encode(input.colors, ops, ops.empty()));

                    return builder;
                }

                @Override
                public <T> Stream<T> keys(DynamicOps<T> ops) {
                    return Stream.of(ops.createString("points"), ops.createString("colors"));
                }
            };
            public List<Vec3> points;
            public List<Integer> colors;

            public Lines(List<Vec3> pPoints, List<Integer> pColors) {
                points = pPoints;
                colors = pColors;
            }

            public Lines(Vec3 start, Vec3 end) {
                points = new ArrayList<>();
                points.add(start);
                points.add(end);
                colors = new ArrayList<>();
            }

            public Lines(Vec3 start, Vec3 end, int color) {
                points = new ArrayList<>();
                points.add(start);
                points.add(end);
                colors = new ArrayList<>();
                colors.add(color);
                colors.add(color);
            }
        }

        public static class Box extends Shape {
            public static final MapCodec<Box> CODEC = new MapCodec<Box>() {

                @Override
                public <T> DataResult<Box> decode(DynamicOps<T> ops, MapLike<T> input) {
                    Vec3 origin = Vec3.CODEC.decode(ops, input.get("origin")).getOrThrow().getFirst();
                    Vec3 size = Vec3.CODEC.decode(ops, input.get("size")).getOrThrow().getFirst();
                    int color = Codec.INT.decode(ops, input.get("color")).getOrThrow().getFirst();

                    return DataResult.success(new Box(origin, size, color));
                }

                @Override
                public <T> RecordBuilder<T> encode(Box input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                    RecordBuilder<T> builder = ops.mapBuilder();

                    builder.add("origin", Vec3.CODEC.encode(input.origin, ops, ops.empty()));
                    builder.add("size", Vec3.CODEC.encode(input.size, ops, ops.empty()));
                    builder.add("color", Codec.INT.encode(input.color, ops, ops.empty()));

                    return builder;
                }

                @Override
                public <T> Stream<T> keys(DynamicOps<T> ops) {
                    return Stream.of(ops.createString("origin"), ops.createString("size"), ops.createString("color"));
                }
            };

            public Vec3 origin;
            public Vec3 size;
            public int color;

            public Box(Vec3 pOrigin, Vec3 pSize) {
                origin = pOrigin;
                size = pSize;
                color = 0xff0000ff;
            }

            public Box(Vec3 pOrigin, Vec3 pSize, int pColor) {
                origin = pOrigin;
                size = pSize;
                color = pColor;
            }


            public Box(Vec3i pPosition) {
                origin = Vec3.atCenterOf(pPosition);
                size = new Vec3(1, 1, 1);
                color = 0xff0000ff;
            }

            public Box(Vec3i pPosition, Vec3 pSize) {
                origin = Vec3.atCenterOf(pPosition);
                size = pSize;
                color = 0xff0000ff;
            }

            public Box setSize(Vec3 pSize){
                size = pSize;
                return this;
            }

            public Box setColor(int pColor){
                color = pColor;
                return this;
            }
        }
    }

    public static class KDebugPayload implements CustomPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, KDebugPayload> STREAM_CODEC = CustomPacketPayload
                .codec(KDebugPayload::write, KDebugPayload::new);
        public static final Type<KDebugPayload> TYPE = new Type<>(ScalmythAPI.rl("kdebug"));
        public Shape shape;

        public KDebugPayload(FriendlyByteBuf buffer) {
            try {
                Tag tag = NbtIo.readAnyTag(new ByteBufInputStream(buffer), NbtAccounter.unlimitedHeap());
                ScalmythAPI.LOGGER.info("TAG: {}", tag.toString());
                shape = Shape.CODEC.compressedDecode(NbtOps.INSTANCE, tag).getOrThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public KDebugPayload(Shape pShape) {
            shape = pShape;
        }

        public void write(FriendlyByteBuf buffer) {
            try {
                Tag tag = Shape.CODEC.encoder().encodeStart(NbtOps.INSTANCE, shape).getOrThrow();
                NbtIo.writeAnyTag(tag, new ByteBufOutputStream(buffer));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
