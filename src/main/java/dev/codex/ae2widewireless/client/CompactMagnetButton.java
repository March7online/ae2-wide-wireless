package dev.codex.ae2widewireless.client;

import net.minecraft.client.gui.GuiGraphics;

import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.gui.IconButton;

/** Draws the magnet-card control inside the compact 12x12 JSON bounds. */
public final class CompactMagnetButton extends IconButton {
    private static final int SIZE = 12;

    public CompactMagnetButton(OnPress onPress) {
        super(onPress,
                Icon.MAGNET,
                Icon.BUTTON_BACKGROUND,
                Icon.BUTTON_BACKGROUND_HOVERED,
                Icon.BUTTON_BACKGROUND_FOCUSED);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        boolean hovered = isHovered();
        Icon background = hovered
                ? Icon.BUTTON_BACKGROUND_HOVERED
                : (isFocused() ? Icon.BUTTON_BACKGROUND_FOCUSED : Icon.BUTTON_BACKGROUND);
        background.getBlitter()
                .dest(getX(), getY(), SIZE, SIZE)
                .zOffset(2)
                .blit(guiGraphics);
        Icon.MAGNET.getBlitter()
                .dest(getX() + 1, getY() + 1, SIZE - 2, SIZE - 2)
                .zOffset(3)
                .blit(guiGraphics);
    }
}
