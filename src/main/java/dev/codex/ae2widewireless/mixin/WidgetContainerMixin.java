package dev.codex.ae2widewireless.mixin;

import java.util.Map;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.IResizableWidget;
import dev.codex.ae2widewireless.WidgetContainerStyleBridge;

@Mixin(WidgetContainer.class)
public abstract class WidgetContainerMixin implements WidgetContainerStyleBridge {
    @Shadow
    @Final
    @Mutable
    private ScreenStyle style;
    @Shadow
    @Final
    private Map<String, AbstractWidget> widgets;
    @Shadow
    @Final
    private Map<String, ICompositeWidget> compositeWidgets;

    @Override
    public void ae2Wide$setStyle(ScreenStyle newStyle, Rect2i absoluteBounds, Rect2i relativeBounds) {
        style = newStyle;
        for (var entry : widgets.entrySet()) {
            var widget = entry.getValue();
            WidgetStyle widgetStyle = newStyle.getWidget(entry.getKey());
            int width = widgetStyle.getWidth() != 0 ? widgetStyle.getWidth() : widget.getWidth();
            int height = widgetStyle.getHeight() != 0 ? widgetStyle.getHeight() : widget.getHeight();
            if (widget instanceof IResizableWidget resizable) {
                resizable.resize(width, height);
            } else {
                widget.setWidth(width);
                widget.setHeight(height);
            }

            var position = widgetStyle.resolve(absoluteBounds);
            if (widget instanceof IResizableWidget resizable) {
                resizable.move(position);
            } else {
                widget.setX(position.getX());
                widget.setY(position.getY());
            }
        }
        for (var entry : compositeWidgets.entrySet()) {
            WidgetStyle widgetStyle = newStyle.getWidget(entry.getKey());
            var composite = entry.getValue();
            composite.setSize(widgetStyle.getWidth(), widgetStyle.getHeight());
            composite.setPosition(widgetStyle.resolve(relativeBounds));
        }
    }
}
