package dev.codex.ae2widewireless;

import net.minecraft.client.renderer.Rect2i;

import appeng.client.gui.style.ScreenStyle;

public interface WidgetContainerStyleBridge {
    void ae2Wide$setStyle(ScreenStyle style, Rect2i absoluteBounds, Rect2i relativeBounds);
}
