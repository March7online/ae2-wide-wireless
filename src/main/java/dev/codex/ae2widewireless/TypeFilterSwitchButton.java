package dev.codex.ae2widewireless;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.gui.widgets.ITooltip;

/** A compact type-filter switch matching WCWT's top-bar controls. */
public class TypeFilterSwitchButton extends Button implements ITooltip {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("ae2", "textures/guis/checkbox.png");

    private final List<Component> tooltip;
    private boolean checked;

    public TypeFilterSwitchButton(OnPress onPress, List<Component> tooltip) {
        super(0, 0, 22, 12, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.tooltip = tooltip;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setVisibility(boolean visible) {
        this.visible = visible;
        this.active = visible;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        int sourceX = isHovered() ? 22 : 0;
        int sourceY = checked ? 40 : 28;
        guiGraphics.blit(TEXTURE, getX(), getY(), width, height,
                sourceX, sourceY, 22, 12, 64, 64);
    }

    @Override
    public List<Component> getTooltipMessage() {
        return tooltip;
    }

    @Override
    public Rect2i getTooltipArea() {
        return new Rect2i(getX(), getY(), width, height);
    }

    @Override
    public boolean isTooltipAreaVisible() {
        return visible;
    }
}
