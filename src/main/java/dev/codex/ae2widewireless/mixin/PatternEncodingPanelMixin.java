package dev.codex.ae2widewireless.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.CraftingEncodingPanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import appeng.client.gui.me.items.SmithingTableEncodingPanel;
import appeng.client.gui.me.items.StonecuttingEncodingPanel;

@Mixin({
        CraftingEncodingPanel.class,
        ProcessingEncodingPanel.class,
        SmithingTableEncodingPanel.class,
        StonecuttingEncodingPanel.class
})
public abstract class PatternEncodingPanelMixin extends EncodingModePanel {
    protected PatternEncodingPanelMixin(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
    }

    @ModifyConstant(
            method = "drawBackgroundLayer",
            constant = @Constant(intValue = 8),
            require = 1)
    private int ae2Wide$centerPatternPanelBackground(int original) {
        if (ae2Wide$isRewrittenUniversalTerminal()) {
            // Keep the native 124 px pattern panel in the right half of the
            // permanent dual workspace. Its slots use the same +169 px offset.
            return 177;
        }
        if (ae2Wide$isWirelessUniversalTerminal()) {
            return 162;
        }
        return screen.getStyle().getTerminalStyle().getSlotsPerRow() > 9 ? 89 : 8;
    }

    @Unique
    private boolean ae2Wide$isRewrittenUniversalTerminal() {
        return screen.getClass().getName()
                .equals("dev.codex.ae2widewireless.client.WideUniversalScreen");
    }

    @Unique
    private boolean ae2Wide$isWirelessUniversalTerminal() {
        if (!screen.getClass().getName().equals("de.mari_023.ae2wtlib.wet.WETScreen")) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(screen.getMenu().getClass().getMethod("isWUT")
                    .invoke(screen.getMenu()));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
