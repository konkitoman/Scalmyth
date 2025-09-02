package dev.nyxane.mods.scalmyth.extrastuff;

import dev.nyxane.mods.scalmyth.api.ScalmythAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;

/**
 * Networking helper for ObjImporter.
 *
 * Now supports doubles for coords, Euler angles for rotation, and scale.
 */
public final class ObjImporterNetworking {

    private ObjImporterNetworking() {}

    public static final CustomPacketPayload.Type<PlaceModelPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("scalmyth", "place_model"));

    public static final StreamCodec<FriendlyByteBuf, PlaceModelPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        ByteBufCodecs.STRING_UTF8.encode(buf, packet.name());
                        buf.writeDouble(packet.x());
                        buf.writeDouble(packet.y());
                        buf.writeDouble(packet.z());
                        buf.writeFloat(packet.scale());
                        buf.writeFloat(packet.rotX());
                        buf.writeFloat(packet.rotY());
                        buf.writeFloat(packet.rotZ());
                    },
                    buf -> new PlaceModelPacket(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat()
                    )
            );

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ScalmythAPI.MOD_ID);
        registrar.playToClient(TYPE, STREAM_CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                try {
                    ObjImporter.ModelPackage modelPackage = ObjImporter.loadModel(packet.name());
                    ObjImporter.ModelGeometry geom = ObjImporter.buildModelGeometryAsQuads(
                            modelPackage,
                            packet.scale(),
                            packet.origin(),
                            packet.rotY(),
                            packet.rotZ(),
                            packet.rotX()
                    );
                    String key = ObjImporter.registryKeyFor(packet.name(), packet.origin());
                    ObjImporter.storeReceivedGeometry(key, geom);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).exceptionally(t -> {
                t.printStackTrace();
                return null;
            });
        });
    }

    public static final class PlaceModelPacket implements CustomPacketPayload {
        private final String name;
        private final double x, y, z;
        private final float scale;
        private final float rotX, rotY, rotZ;

        public PlaceModelPacket(String name, double x, double y, double z, float scale, float rotX, float rotY, float rotZ) {
            this.name = Objects.requireNonNull(name);
            this.x = x; this.y = y; this.z = z;
            this.scale = scale;
            this.rotX = rotX; this.rotY = rotY; this.rotZ = rotZ;
        }

        public String name() { return name; }
        public double x() { return x; }
        public double y() { return y; }
        public double z() { return z; }
        public float scale() { return scale; }
        public float rotX() { return rotX; }
        public float rotY() { return rotY; }
        public float rotZ() { return rotZ; }

        public BlockPos origin() {
            return BlockPos.containing(x, y, z);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void sendPlaceModelToAllClients(String modelName, double x, double y, double z, float scale, float rotX, float rotY, float rotZ) {
        PacketDistributor.sendToAllPlayers(new PlaceModelPacket(modelName, x, y, z, scale, rotX, rotY, rotZ));
    }
}
