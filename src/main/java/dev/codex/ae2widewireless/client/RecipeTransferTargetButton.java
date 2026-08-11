package dev.codex.ae2widewireless.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import appeng.client.gui.widgets.ITooltip;

import de.mari_023.ae2wtlib.api.gui.Icon;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

/** Selects which of the two permanent work areas receives JEI recipes. */
public final class RecipeTransferTargetButton extends Button implements ITooltip {
    private final WideUniversalMenu menu;

    public RecipeTransferTargetButton(WideUniversalMenu menu) {
        super(0, 0, 16, 16, Component.empty(), button -> menu.toggleRecipeTransferTarget(),
                Button.DEFAULT_NARRATION);
        this.menu = menu;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int yOffset = isHovered() ? 1 : 0;
        var background = isHovered()
                ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVERED
                : isFocused()
                        ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUSED
                        : Icon.TOOLBAR_BUTTON_BACKGROUND;
        background.getBlitter()
                .dest(getX() - 1, getY() + yOffset, background.width(), background.height())
                .zOffset(2)
                .blit(guiGraphics);

        int centerY = getY() + height / 2 + yOffset;
        int color = active ? 0xFFFFFFFF : 0xFFA0A0A0;
        boolean pointsLeft = menu.isRecipeTransferToManualGrid();
        int tailX = pointsLeft ? getX() + width - 3 : getX() + 3;
        int tipX = pointsLeft ? getX() + 3 : getX() + width - 3;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 3);
        try {
            guiGraphics.hLine(Math.min(tailX, tipX), Math.max(tailX, tipX), centerY, color);
            for (int i = 0; i < 4; i++) {
                int x = pointsLeft ? tipX + i : tipX - i;
                guiGraphics.fill(x, centerY - i, x + 1, centerY - i + 1, color);
                guiGraphics.fill(x, centerY + i, x + 1, centerY + i + 1, color);
            }
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    @Override
    public List<Component> getTooltipMessage() {
        return List.of(
                Component.translatable("gui.ae2_wide_wireless.jei_target"),
                Component.translatable(menu.isRecipeTransferToManualGrid()
                        ? "gui.ae2_wide_wireless.jei_target.manual"
                        : "gui.ae2_wide_wireless.jei_target.pattern"),
                Component.translatable("gui.ae2_wide_wireless.jei_target.hint"));
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX() - 1, getY(), 18, 20);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }
}
