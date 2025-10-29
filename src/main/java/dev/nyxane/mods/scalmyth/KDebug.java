package dev.nyxane.mods.scalmyth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.selector.EntitySelector;
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
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

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

            shape.render(matrix, bufferSource, camX, camY, camZ);

            shape.time -= delta;
            boolean res = shape.time < 0;
            if (res) shape.clean();
            return res;
        });
        SHAPES_LOCK.unlockWrite(stamp);

        poseStack.popPose();
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

        public void render(Matrix4f matrix, MultiBufferSource bufferSource, double camX, double camY,
                           double camZ) {
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

            @Override
            public void render(Matrix4f matrix, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
                Random RANDOM = new Random(id);
                VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugLineStrip(1));

                Vec3 first = points.getLast();
                buffer.addVertex(matrix, (float) (first.x - camX), (float) (first.y - camY),
                    (float) (first.z - camZ));
                buffer.setColor(0);

                for (int i = 0; i < points.size(); i++) {
                    Vec3 point = points.get(i);
                    int color = colors.get(i);
                    if (color == 0) {
                        color = RANDOM.nextInt();
                    }
                    buffer.addVertex(matrix, (float) (point.x - camX), (float) (point.y - camY),
                        (float) (point.z - camZ));
                    buffer.setColor(color);
                }

                Vec3 last = points.getLast();

                buffer.addVertex(matrix, (float) (last.x - camX), (float) (last.y - camY),
                    (float) (last.z - camZ));
                buffer.setColor(0);
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

            @Override
            public void render(Matrix4f matrix, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
                Random RANDOM = new Random(id);
                int color = this.color;
                if (color == 0) {
                    color = RANDOM.nextInt() | 0xff000000;
                }
                drawBox(matrix, bufferSource, origin, size, color, new Vec3(camX, camY, camZ));
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

    private static void drawBox(Matrix4f matrix, MultiBufferSource bufferSource, Vec3 origin, Vec3 size, int color,
                                Vec3 cam) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugFilledBox());

        double sx = size.x / 2;
        double sy = size.y / 2;
        double sz = size.z / 2;

        // Hack, for not losing faces, because we are rendering in triangle strip.
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(0x00000000);
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, -sy - cam.y, sz - cam.z).toVector3f()));
        buffer.setColor(0x00000000);

        // TODO: find a better way to draw a box/voxel with triangle strip.
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

        // Hack, for not losing faces, because we are rendering in triangle strip.
        buffer.addVertex(matrix.transformPosition(origin.add(-sx - cam.x, sy - cam.y, -sz - cam.z).toVector3f()));
        buffer.setColor(0x00000000);
    }

    // We need to access all the Shapes for the codecs to register.
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
            .then(Commands.literal("discard").then(Commands.argument("entities", EntityArgument.entities()).executes(context -> {
                int count = 0;
                for (Entity entity : EntityArgument.getEntities(context, "entities")) {
                    context.getSource().sendSystemMessage(Component.literal(String.format("discarding: %s : %s", entity.getType(), entity.getStringUUID())));
                    if (entity instanceof Player) {
                        context.getSource().sendSystemMessage(Component.literal(String.format("Player %s will not be discarded!", entity.getName().getString())));
                        continue;
                    }
                    entity.discard();
                    count += 1;
                }
                context.getSource().sendSystemMessage(Component.literal(String.format("discarded: %d", count)));
                return 0;
            })))
        );
    }

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext pContext) {
        LiteralArgumentBuilder<CommandSourceStack> commands = Commands.literal("kdebug-client");
        commands.then(Commands.literal("enable").executes(s -> {
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
            .then(registerGeckolibCommands());


        dispatcher.register(commands);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerGeckolibCommands() {
        return Commands.literal("geckolib")
            .then(Commands.literal("play")
                .then(Commands.argument("entity", EntityArgument.entity()).executes(context -> {
                        Entity entity = ClientSided.getEntity((CommandContext<ClientCommandSourceStack>) (Object) context, "entity");

                        if (entity instanceof GeoEntity geoEntity) {
                            EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                            if (entityRenderer instanceof GeoEntityRenderer<?> geoEntityRenderer) {
                                GeoModel<GeoEntity> model = (GeoModel<GeoEntity>) geoEntityRenderer.getGeoModel();
                                ResourceLocation location = model.getAnimationResource(geoEntity);
                                BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);
                                context.getSource().sendSystemMessage(Component.literal("Animations:"));
                                bakedAnimations.animations().forEach((name, animation) -> {
                                    context.getSource().sendSystemMessage(Component.literal(name));
                                });
                                return 1;
                            }
                        }

                        return 0;
                    }).then(Commands.argument("animation", new AnimationArgument("entity")).executes(context -> {
                        Entity entity = ClientSided.getEntity((CommandContext<ClientCommandSourceStack>) (Object) context, "entity");
                        String animation = AnimationArgument.getAnimation(context, "animation");


                        if (entity instanceof GeoEntity geoEntity) {
                            AnimatableManager<?> manager = geoEntity.getAnimatableInstanceCache().getManagerForId(entity.getId());

                            EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                            if (entityRenderer instanceof GeoEntityRenderer<?> geoEntityRenderer) {
                                GeoModel<GeoEntity> model = (GeoModel<GeoEntity>) geoEntityRenderer.getGeoModel();
                                ResourceLocation location = model.getAnimationResource(geoEntity);
                                BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);
                                if (!bakedAnimations.animations().containsKey(animation)) {
                                    context.getSource().sendFailure(Component.literal(String.format("Cannot find animation: %s", animation)));
                                    return 0;
                                }
                            }

                            manager.getAnimationControllers().clear();

                            manager.addController(new AnimationController(geoEntity, "kdebug", 1,
                                event -> event.setAndContinue(RawAnimation.begin().thenPlay(animation)
                                )));
                            return 1;
                        } else {
                            context.getSource().sendFailure(Component.literal("Is not a geckolib entity!"));
                        }

                        return 0;
                    }))
                )).then(Commands.literal("stop")
                .then(Commands.argument("entity", EntityArgument.entity()).executes(context -> {
                    Entity entity = ClientSided.getEntity((CommandContext<ClientCommandSourceStack>) (Object) context, "entity");
                    if (entity instanceof GeoEntity geoEntity) {
                        AnimatableManager<?> manager = geoEntity.getAnimatableInstanceCache().getManagerForId(entity.getId());
                        manager.removeController("kdebug");

                        AnimatableManager.ControllerRegistrar registrar = new AnimatableManager.ControllerRegistrar(new ArrayList<>());
                        geoEntity.registerControllers(registrar);
                        registrar.controllers().forEach(manager::addController);

                        return 1;
                    }

                    return 0;
                })));
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
                Path path = e.getNavigation().createPath(pos, 0);
                e.getNavigation().moveTo(path, speed);
            } else {
                context.getSource().sendFailure(Component.literal(String.format("Failed for %s %s", entity.getType(), entity.getUUID())));
            }
        }
        return 0;
    }

    public record AnimationSelector(String animation) {
    }

    public static class AnimationArgument implements ArgumentType<AnimationSelector> {
        String entityArgument;

        public AnimationArgument(String pEntityArgument) {
            entityArgument = pEntityArgument;
        }

        public static <S> String getAnimation(CommandContext<S> context, String argument) {
            return context.getArgument(argument, AnimationSelector.class).animation;
        }

        @Override
        public AnimationSelector parse(StringReader reader) throws CommandSyntaxException {
            return new AnimationSelector(reader.readString());
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            CommandContext<ClientCommandSourceStack> newC = (CommandContext<ClientCommandSourceStack>) context.copyFor((S) ClientCommandHandler.getSource());

            Entity entity = null;
            try {
                entity = ClientSided.getEntity(newC, entityArgument);
            } catch (Exception e) {
                return builder.buildFuture();
            }

            if (entity instanceof GeoEntity geoEntity) {
                EntityRenderer<?> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
                if (entityRenderer instanceof GeoEntityRenderer<?> geoEntityRenderer) {
                    GeoModel<GeoEntity> model = (GeoModel<GeoEntity>) geoEntityRenderer.getGeoModel();
                    ResourceLocation location = model.getAnimationResource(geoEntity);
                    BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);
                    bakedAnimations.animations().forEach((name, animation) -> {
                        builder.suggest(name);
                    });
                }
            }

            return builder.buildFuture();
        }
    }

    /// This is needed because {@link EntityArgument#getEntity} is not working on the client side.
    private static class ClientSided {
        /// Will mostly work the same as {@link EntityArgument#getEntity}, but will only find entities in the current dimension.
        static Entity getEntity(CommandContext<ClientCommandSourceStack> context, String name) throws CommandSyntaxException {
            try {
                return findSingleEntity(context.getArgument(name, EntitySelector.class), context.getSource());
            } catch (CommandSyntaxException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        static Entity findSingleEntity(EntitySelector selector, CommandSourceStack source) throws CommandSyntaxException, NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
            List<? extends Entity> list = findEntities(selector, source);
            if (list.isEmpty()) {
                throw EntityArgument.NO_ENTITIES_FOUND.create();
            } else if (list.size() > 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            } else {
                return list.getFirst();
            }
        }

        static List<? extends Entity> findEntities(EntitySelector selector, CommandSourceStack source) throws CommandSyntaxException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            Field field$position = EntitySelector.class.getDeclaredField("position");
            field$position.setAccessible(true);
            Function<Vec3, Vec3> position = (Function<Vec3, Vec3>) field$position.get(selector);
            Method method$getAbsoluteAabb = EntitySelector.class.getDeclaredMethod("getAbsoluteAabb", Vec3.class);
            method$getAbsoluteAabb.setAccessible(true);
            Function<Vec3, AABB> getAbsoluteAabb = (pos) -> {
                try {
                    return (AABB) method$getAbsoluteAabb.invoke(selector, pos);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            Field field$currentEntity = EntitySelector.class.getDeclaredField("currentEntity");
            field$currentEntity.setAccessible(true);
            boolean currentEntity = field$currentEntity.getBoolean(selector);
            Method method$getPredicate = EntitySelector.class.getDeclaredMethod("getPredicate", Vec3.class, AABB.class, FeatureFlagSet.class);
            method$getPredicate.setAccessible(true);
            Function3<Vec3, AABB, FeatureFlagSet, Predicate<Entity>> getPredicate = (a, b, c) -> {
                try {
                    return (Predicate<Entity>) method$getPredicate.invoke(selector, a, b, c);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            Method method$sortAndLimit = EntitySelector.class.getDeclaredMethod("sortAndLimit", Vec3.class, List.class);
            method$sortAndLimit.setAccessible(true);
            BiFunction<Vec3, List<? extends Entity>, List<? extends Entity>> sortAndLimit = (a, b) -> {
                try {
                    return (List<? extends Entity>) method$sortAndLimit.invoke(selector, a, b);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            Field field$entityUUID = EntitySelector.class.getDeclaredField("entityUUID");
            field$entityUUID.setAccessible(true);
            UUID entityUUID = (UUID) field$entityUUID.get(selector);

            Field field$entityStorage = ClientLevel.class.getDeclaredField("entityStorage");
            field$entityStorage.setAccessible(true);

            if (!selector.includesEntities()) {
                return selector.findPlayers(source);
            } else if (entityUUID != null) {
                if (source.getUnsidedLevel() instanceof ClientLevel level) {
                    TransientEntitySectionManager<Entity> e = (TransientEntitySectionManager<Entity>) field$entityStorage.get(level);
                    Entity entity = e.getEntityGetter().get(entityUUID);
                    if (entity != null) {
                        return List.of(entity);
                    }
                }

                return List.of();
            } else {
                Vec3 vec3 = (Vec3) position.apply(source.getPosition());
                AABB aabb = getAbsoluteAabb.apply(vec3);
                if (currentEntity) {
                    Predicate<Entity> predicate1 = getPredicate.apply(vec3, aabb, (FeatureFlagSet) null);
                    return source.getEntity() != null && predicate1.test(source.getEntity()) ? List.of(source.getEntity()) : List.of();
                } else {
                    Predicate<Entity> predicate = getPredicate.apply(vec3, aabb, null);
                    List<Entity> list = new ObjectArrayList();
                    if (source.getUnsidedLevel() instanceof ClientLevel level) {
                        addEntities(selector, list, level, aabb, predicate);
                    }

                    return sortAndLimit.apply(vec3, list);
                }
            }
        }

        static void addEntities(EntitySelector selector, List<Entity> entities, ClientLevel level, @Nullable AABB box, Predicate<Entity> predicate) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
            Method method$getResultLimit = EntitySelector.class.getDeclaredMethod("getResultLimit");
            method$getResultLimit.setAccessible(true);
            Field field$type = EntitySelector.class.getDeclaredField("type");
            field$type.setAccessible(true);
            EntityTypeTest<Entity, ?> type = (EntityTypeTest<Entity, ?>) field$type.get(selector);

            int i = (int) method$getResultLimit.invoke(selector);
            if (entities.size() < i) {
                if (box != null) {
                    level.getEntities(type, box, predicate, entities, i);
                } else {

                    Field field$entityStorage = ClientLevel.class.getDeclaredField("entityStorage");
                    field$entityStorage.setAccessible(true);
                    TransientEntitySectionManager<Entity> e = (TransientEntitySectionManager<Entity>) field$entityStorage.get(level);
                    e.getEntityGetter().get(type, (p_261428_) -> {
                        if (predicate.test(p_261428_)) {
                            entities.add(p_261428_);
                            if (entities.size() >= i) {
                                return AbortableIterationConsumer.Continuation.ABORT;
                            }
                        }

                        return AbortableIterationConsumer.Continuation.CONTINUE;
                    });
                }
            }
        }
    }
}
