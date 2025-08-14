package dev.nyxane.mods.scalmyth.extrastuff;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TexturedButton extends AbstractButton {

    @FunctionalInterface
    public interface ButtonCallback {
        void onPress(TexturedButton button);
    }

    private final ButtonCallback callback;
    private final ResourceLocation texture;
    private final int u, v;
    private final int textureWidth, textureHeight;

    public TexturedButton(int x, int y, int width, int height, Component message,
                          ButtonCallback callback, ResourceLocation texture,
                          int u, int v, int textureWidth, int textureHeight) {
        super(x, y, width, height, message);
        this.callback = callback;
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void onPress() {
        if (callback != null) callback.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draw the button texture
        graphics.blit(texture, getX(), getY(), u, v, getWidth(), getHeight(), textureWidth, textureHeight);

        // Draw centered text
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(getMessage());
        graphics.drawString(font, getMessage(),
                getX() + getWidth() / 2 - textWidth / 2,
                getY() + (getHeight() - 8) / 2, 0xFFFFFF, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }
}
