package dev.nyxane.mods.scalmyth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;

public class KDebug {
    private static final ArrayList<Shape> SHAPES = new ArrayList<>();
    private static final StampedLock SHAPES_LOCK = new StampedLock();
    private static Instant RENDER_LAST_TIME = Instant.now();
    private static Instant TICK_LAST_TIME = Instant.now();
    private static final AtomicInteger NEXT = new AtomicInteger((int) Math.pow(2, 16));
    private static boolean ENABLED = false;

    public static void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY,
                              double camZ) {
        poseStack.pushPose();
        Matrix4f matrix = poseStack.last().pose();

        Duration elapsed = Duration.between(RENDER_LAST_TIME, Instant.now());
        RENDER_LAST_TIME = Instant.now();
        float delta = (float) (((double) elapsed.getNano() / Math.pow(10.0, 9)) + (double) elapsed.getSeconds());

        long stamp = SHAPES_LOCK.writeLock();
        SHAPES.removeIf(shape -> {
            if (!shape.isClient()) return false;

            if (!ENABLED) {
                shape.clean();
                return true;
            }

            Random RANDOM = new Random(shape.id);
            switch (shape) {
                case Shape.Lines lines -> {
                    VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugLineStrip(1));

                    Vec3 first = lines.points.getLast();
                    buffer.addVertex(matrix, (float) (first.x - camX), (float) (first.y - camY),
                        (float) (first.z - camZ));
                    buffer.setColor(0);

                    for (int i = 0; i < lines.points.size(); i++) {
                        Vec3 point = lines.points.get(i);
                        int color = lines.colors.get(i);
                        if (color == 0) {
                            color = RANDOM.nextInt();
                        }
                        buffer.addVertex(matrix, (float) (point.x - camX), (float) (point.y - camY),
                            (float) (point.z - camZ));
                        buffer.setColor(color);
                    }

                    Vec3 last = lines.points.getLast();

                    buffer.addVertex(matrix, (float) (last.x - camX), (float) (last.y - camY),
                        (float) (last.z - camZ));
                    buffer.setColor(0);
                }
                case Shape.Box box -> {
                    int color = box.color;
                    if (color == 0) {
                        color = RANDOM.nextInt() | 0xff000000;
                    }
                    drawBox(matrix, bufferSource, box.origin, box.size, color, new Vec3(camX, camY, camZ));
                }
                default -> {
                }
            }

            shape.time -= delta;
            boolean res = shape.time < 0;
            if (res) shape.clean();
            return res;
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
        if (!ENABLED) return;
        if (level instanceof ServerLevel serverLevel) {
            if (!shape.isClient()) {
                if (addShapeOrReplace(shape)) {
                    if (shape instanceof Shape.Entity entity) {
                        level.addFreshEntity(entity.getEntity(level));
                    }
                }

                return;
            }

            for (ServerPlayer player : serverLevel.players()) {
                player.connection.send(new KDebugPayload(shape));
            }
            return;
        } else if (!shape.isClient() && level instanceof ClientLevel) {
            level.sendPacketToServer(new ServerboundCustomPayloadPacket(new KDebugPayload(shape)));
            return;
        }

        addShapeOrReplace(shape);
    }

    private static boolean addShapeOrReplace(Shape shape) {
        long stamp = SHAPES_LOCK.writeLock();

        for (int i = 0; i < SHAPES.size(); i++) {
            Shape old = SHAPES.get(i);
            if (old.id == shape.id) {
                if (old instanceof Shape.Entity oldEntity) {
                    if (shape instanceof Shape.Entity newEntity && oldEntity.data.getString("id").equals(newEntity.data.getString("id"))) {
                        newEntity.entity = oldEntity.entity;
                        newEntity.entity.load(newEntity.data);
                    } else {
                        old.clean();
                        SHAPES.set(i, shape);
                        SHAPES_LOCK.unlockWrite(stamp);
                        return true;
                    }
                }

                SHAPES.set(i, shape);
                SHAPES_LOCK.unlockWrite(stamp);
                return false;
            }
        }

        if (shape.id == 0) {
            shape.id = NEXT.getAndAdd(1);
        }

        SHAPES.add(shape);
        SHAPES_LOCK.unlockWrite(stamp);
        return true;
    }

    public static abstract class Shape {
        private static final LinkedHashMap<String, MapCodec<Shape>> CODECS = new LinkedHashMap<>();

        public static <S extends Shape> MapCodec<Shape> register(String variant, MapCodec<S> codec) {
            if (CODECS.get(variant) != null)
                throw new RuntimeException(String.format("KDebug The variant `%s` is already registered", variant));

            CODECS.put(variant, (MapCodec<Shape>) codec);
            return CODEC;
        }

        public static final MapCodec<Shape> CODEC = RecordCodecBuilder.mapCodec(
            (i) -> i.group(
                Codec.FLOAT.fieldOf("time").forGetter(s -> s.time),
                Codec.INT.fieldOf("id").forGetter(s -> s.id),
                Codec.STRING.fieldOf("variant").forGetter(Shape::variant),
                CompoundTag.CODEC.fieldOf("inner").forGetter(s -> {
                    Tag tag = CODECS.get(s.variant()).encoder().encodeStart(NbtOps.INSTANCE, s).getOrThrow();
                    if (tag instanceof CompoundTag compoundTag) {
                        return compoundTag;
                    } else {
                        throw new RuntimeException("in not CompoundTag");
                    }
                })).apply(i, Shape::decode));

        private static Shape decode(float time, int id, String variant, CompoundTag inner) {
            MapCodec<Shape> codec = CODECS.get(variant);
            if (codec == null)
                throw new RuntimeException(String.format("KDebug: a codec could not be found for variant: `%s`", variant));

            Shape shape = codec.compressedDecode(NbtOps.INSTANCE, inner).getOrThrow();

            shape.time = time;
            shape.id = id;

            return shape;
        }

        protected abstract String variant();

        float time = 1;
        int id = 0;

        public Shape setTime(float pTimme) {
            time = pTimme;
            return this;
        }

        public Shape setId(Object pId) {
            id = pId.hashCode();
            return this;
        }

        public boolean isClient() {
            return true;
        }

        public void clean() {
        }

        public static class Lines extends Shape {
            public static final String VARIANT = "lines";
            public static final MapCodec<Shape> CODEC = Shape
                .register(VARIANT, RecordCodecBuilder.<Lines>mapCodec(
                    (i) -> i.group(
                        Vec3.CODEC.listOf().fieldOf("points").forGetter(l -> l.points),
                        Codec.INT.listOf().fieldOf("colors").forGetter(l -> l.colors)
                    ).apply(i, Lines::new)));

            @Override
            protected String variant() {
                return VARIANT;
            }

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
                colors.add(0);
                colors.add(0);
            }

            public Lines(Vec3 start, Vec3 end, int color) {
                points = new ArrayList<>();
                points.add(start);
                points.add(end);
                colors = new ArrayList<>();
                colors.add(color);
                colors.add(color);
            }

            public Lines(Vec3 start, int start_color, Vec3 end, int end_color) {
                points = new ArrayList<>();
                points.add(start);
                points.add(end);
                colors = new ArrayList<>();
                colors.add(start_color);
                colors.add(end_color);
            }

            public Lines addPoint(Vec3 point) {
                points.add(point);
                colors.add(0);
                return this;
            }

            public Lines addPoint(Vec3 point, int color) {
                points.add(point);
                colors.add(color);
                return this;
            }

            public Lines setColor(int color) {
                assert !points.isEmpty();
                colors.add(points.size() - 1, color);

                return this;
            }
        }

        public static class Box extends Shape {
            public static final String VARIANT = "box";
            public static final MapCodec<Shape> CODEC = Shape
                .register(VARIANT, RecordCodecBuilder.<Box>mapCodec(
                    (i) -> i.group(
                        Vec3.CODEC.fieldOf("origin").forGetter(b -> b.origin),
                        Vec3.CODEC.fieldOf("size").forGetter(b -> b.size),
                        Codec.INT.fieldOf("color").forGetter(b -> b.color)
                    ).apply(i, Box::new)));

            @Override
            protected String variant() {
                return VARIANT;
            }

            public Vec3 origin;
            public Vec3 size;
            public int color;

            public Box(Vec3 pOrigin, Vec3 pSize) {
                origin = pOrigin;
                size = pSize;
                color = 0;
            }

            public Box(Vec3 pOrigin, Vec3 pSize, int pColor) {
                origin = pOrigin;
                size = pSize;
                color = pColor;
            }

            public Box(Vec3i pPosition) {
                origin = Vec3.atCenterOf(pPosition);
                size = new Vec3(1, 1, 1);
                color = 0;
            }

            public Box(Vec3i pPosition, Vec3 pSize) {
                origin = Vec3.atCenterOf(pPosition);
                size = pSize;
                color = 0;
            }

            public Box setSize(Vec3 pSize) {
                size = pSize;
                return this;
            }

            public Box setColor(int pColor) {
                color = pColor;
                return this;
            }
        }

        public static class Entity extends Shape {
            public static final String VARIANT = "entity";
            public static final MapCodec<Shape> CODEC = Shape
                .register(VARIANT, RecordCodecBuilder.<Entity>mapCodec(
                    (i) -> i.group(
                        CompoundTag.CODEC.fieldOf("data").forGetter(e -> e.data)
                    ).apply(i, Entity::new)));

            @Override
            protected String variant() {
                return VARIANT;
            }

            CompoundTag data;
            net.minecraft.world.entity.Entity entity;

            public Entity(CompoundTag pData) {
                data = pData;
            }

            public Entity(net.minecraft.world.entity.Entity pEntity) {
                entity = pEntity;
                data = new CompoundTag();
                pEntity.save(data);
            }

            public net.minecraft.world.entity.Entity getEntity(Level level) {
                if (entity != null) {
                    return entity;
                }
                entity = EntityType.loadEntityRecursive(data, level, e -> e);
                return entity;
            }

            @Override
            public void clean() {
                if (entity != null) {
                    entity.discard();
                }
            }

            @Override
            public boolean isClient() {
                return false;
            }
        }
    }

    // If we don't try to access the Shapes here their codecs will not be registered.
    // Because java is lazy!
    static {
        Shape.Lines.CODEC.codec();
        Shape.Box.CODEC.codec();
        Shape.Entity.CODEC.codec();
    }

    public static class KDebugPayload implements CustomPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, KDebugPayload> STREAM_CODEC = CustomPacketPayload
            .codec(KDebugPayload::write, KDebugPayload::new);
        public static final Type<KDebugPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("kdebug", "payload"));
        public Shape shape;

        public KDebugPayload(FriendlyByteBuf buffer) {
            try {
                Tag tag = NbtIo.readAnyTag(new ByteBufInputStream(buffer), NbtAccounter.unlimitedHeap());
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
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> summonCommand(CommandBuildContext pContext) {
        return Commands
            .literal("summon")
            .then(Commands.argument("entity", ResourceArgument.resource(pContext, Registries.ENTITY_TYPE))
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .then(Commands.argument("pos", Vec3Argument.vec3())
                    .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                        .then(Commands.argument("time", FloatArgumentType.floatArg(0))
                            .then(Commands.argument("id", IntegerArgumentType.integer())
                                .executes(context -> {
                                    if (!ENABLED) {
                                        context.getSource().sendFailure(Component.literal("kdebug is not enabled!"));
                                        return 0;
                                    }

                                    EntityType<?> entityType = ResourceArgument.getEntityType(context, "entity").value();
                                    Vec3 pos = Vec3Argument.getVec3(context, "pos");
                                    CompoundTag data = CompoundTagArgument.getCompoundTag(context, "nbt").copy();
                                    data.putString("id", EntityType.getKey(entityType).toString());
                                    float time = FloatArgumentType.getFloat(context, "time");
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    Level level = context.getSource().getUnsidedLevel();
                                    Entity entity = EntityType.loadEntityRecursive(data, level, e -> {
                                        e.moveTo(pos);
                                        return e;
                                    });

                                    if (entity == null) {
                                        context.getSource().sendFailure(Component.literal("Cannot summon entity!"));
                                        return 0;
                                    }

                                    addShape(level, new Shape.Entity(entity).setTime(time).setId(id));

                                    return 0;
                                }))))));
    }

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext pContext) {
        dispatcher.register(Commands.literal("kdebug")
            .requires(p -> p.hasPermission(2))
            .then(Commands.literal("enable").executes(s -> {
                if (!ENABLED) {
                    s.getSource().sendSystemMessage(Component.literal("kdebug Enabled"));
                }
                ENABLED = true;
                return 0;
            }))
            .then(Commands.literal("disable").executes(s -> {
                if (ENABLED) {
                    s.getSource().sendSystemMessage(Component.literal("kdebug Disabled"));
                }
                ENABLED = false;
                return 0;
            }))
            .then(Commands.literal("navigate").then(Commands.argument("target", EntityArgument.entities())
                .then(Commands.argument("to", BlockPosArgument.blockPos())
                    .executes(context -> commandNavigate(context, 1))
                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                        .executes((context) -> commandNavigate(context, DoubleArgumentType.getDouble(context, "speed"))))
                ))
            )
            .then(summonCommand(pContext))
        );
    }

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext pContext) {
        dispatcher.register(Commands.literal("kdebug-client")
            .then(Commands.literal("enable").executes(s -> {
                if (!ENABLED) {
                    s.getSource().sendSystemMessage(Component.literal("kdebug Enabled"));
                }
                ENABLED = true;
                return 0;
            }))
            .then(Commands.literal("disable").executes(s -> {
                if (ENABLED) {
                    s.getSource().sendSystemMessage(Component.literal("kdebug Disabled"));
                }
                ENABLED = false;
                return 0;
            }))
            .then(summonCommand(pContext))
        );
    }

    public static void serverTick() {
        Duration elapsed = Duration.between(TICK_LAST_TIME, Instant.now());
        TICK_LAST_TIME = Instant.now();
        float delta = (float) (((double) elapsed.getNano() / Math.pow(10.0, 9)) + (double) elapsed.getSeconds());

        long stamp = SHAPES_LOCK.writeLock();
        SHAPES.removeIf(shape -> {
            if (shape.isClient()) return false;

            if (!ENABLED) {
                shape.clean();
                return true;
            }

            shape.time -= delta;
            boolean res = shape.time < 0;
            if (res) shape.clean();
            return res;
        });
        SHAPES_LOCK.unlockWrite(stamp);
    }

    public static void clean() {
        long stamp = SHAPES_LOCK.writeLock();
        SHAPES.removeIf(shape -> {
            shape.clean();
            return true;
        });
        SHAPES_LOCK.unlockWrite(stamp);
    }

    public static int commandNavigate(CommandContext<CommandSourceStack> context, double speed) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "to");
        for (Entity entity : EntityArgument.getEntities(context, "target")) {
            if (entity instanceof Mob e) {
                Path path = e.getNavigation().createPath(pos, 1);
                e.getNavigation().moveTo(path, speed);
            } else {
                context.getSource().sendFailure(Component.literal(String.format("Failed for %s %s", entity.getType(), entity.getUUID())));
            }
        }
        return 0;
    }
}
