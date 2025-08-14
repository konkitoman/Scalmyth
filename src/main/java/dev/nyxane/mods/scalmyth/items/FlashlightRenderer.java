package dev.nyxane.mods.scalmyth.items;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.AreaLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FlashlightRenderer extends AreaLight {

    private final AreaLight areaLight2 = new AreaLight();
    private boolean isOn = false;
    private Vec3 lastPosition = new Vec3(0, 0, 0);
    private Quaternionf lastRotation = new Quaternionf();
    private static final float SMOOTH_FACTOR = 0.2f;
    private final Minecraft client = Minecraft.getInstance();

    public FlashlightRenderer() {
        super();
        this.setDistance(50.0f);
        this.setSize(1.0, 1.0);
        this.setColor(1.0f, 0.9f, 0.647f);
        this.setBrightness(1.5f);

        areaLight2.setDistance(50f);
        areaLight2.setSize(0.15, 0.15);
        areaLight2.setBrightness(2.0f);
        areaLight2.setAngle(0.25f);
        areaLight2.setColor(1.0f, 0.9f, 0.641f);
    }

    public void toggle() {
        isOn = !isOn;

        if (isOn) {
            this.setAngle((float) Math.toRadians(35));
            VeilRenderSystem.renderer().getLightRenderer().addLight(this);
            VeilRenderSystem.renderer().getLightRenderer().addLight(areaLight2);
            playOnToggleSound();
        } else {
            VeilRenderSystem.renderer().getLightRenderer().removeLight(this);
            VeilRenderSystem.renderer().getLightRenderer().removeLight(areaLight2);
            playOffToggleSound();
        }
    }

    private void playOnToggleSound() {
        if (client.player != null && client.level != null) {
            client.level.playSound(
                    client.player,
                    client.player.blockPosition(),
                    SoundEvents.STONE_BUTTON_CLICK_ON,
                    SoundSource.PLAYERS,
                    1f,
                    1f
            );
        }
    }
    private void playOffToggleSound() {
        if (client.player != null && client.level != null) {
            client.level.playSound(
                    client.player,
                    client.player.blockPosition(),
                    SoundEvents.STONE_BUTTON_CLICK_OFF,
                    SoundSource.PLAYERS,
                    1f,
                    1f
            );
        }
    }

    public boolean isOn() {
        return isOn;
    }

    public void updateFromHead(Minecraft client, float partialTicks) {
        if (!isOn || client == null || client.player == null) return;

        // Interpolated player eye position
        Vec3 eyePos = new Vec3(
                client.player.xOld + (client.player.getX() - client.player.xOld) * partialTicks,
                client.player.yOld + (client.player.getY() - client.player.yOld) * partialTicks + client.player.getEyeHeight(),
                client.player.zOld + (client.player.getZ() - client.player.zOld) * partialTicks
        );
        lastPosition = eyePos;

        // Player rotation
        float pitch = client.player.getXRot(); // up/down
        float yaw = client.player.getYRot();   // left/right

        Quaternionf targetRotation = new Quaternionf()
                .rotateY((float) Math.toRadians(-yaw))  // correct yaw
                .rotateX((float) Math.toRadians(pitch)); // flip pitch

        lastRotation.slerp(targetRotation, SMOOTH_FACTOR);

        // Forward vector in world space
        Vector3f forward = new Vector3f(0, 0, -1).rotate(lastRotation); // negative Z
        Vector3f up = new Vector3f(0, 1, 0).rotate(lastRotation);
        Quaternionf orientation = new Quaternionf().lookAlong(forward, up);

        // **Offset the lights slightly forward from the eyes**
        float forwardOffset = -1f; // tweak this value to adjust distance
        Vec3 lightPos = lastPosition.add(forward.x() * forwardOffset, forward.y() * forwardOffset, forward.z() * forwardOffset);

        // Apply to lights
        this.setPosition(lightPos.x, lightPos.y, lightPos.z);
        this.setOrientation(orientation);

        areaLight2.setPosition(lightPos.x, lightPos.y, lightPos.z);
        areaLight2.setOrientation(orientation);
    }



}
