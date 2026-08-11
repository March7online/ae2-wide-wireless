package dev.codex.ae2widewireless.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

/** Compact vanilla-style player preview integrated into the inventory band. */
public final class PlayerPreviewWidget extends AbstractWidget {
    private final LivingEntity entity;

    public PlayerPreviewWidget(LivingEntity entity) {
        super(0, 0, 48, 70, Component.empty());
        this.entity = entity;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                getX(), getY(), getX() + width, getY() + height,
                28, 0.0625F,
                mouseX, mouseY,
                entity);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
