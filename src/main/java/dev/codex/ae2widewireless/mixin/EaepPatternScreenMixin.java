package dev.codex.ae2widewireless.mixin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;

@Mixin(value = PatternEncodingTermScreen.class, priority = 900)
public abstract class EaepPatternScreenMixin extends MEStorageScreen<PatternEncodingTermMenu> {
    @Shadow
    @Final
    private Map<EncodingMode, EncodingModePanel> modePanels;

    @Unique
    private static Field[] ae2Wide$eaepButtons;

    protected EaepPatternScreenMixin(PatternEncodingTermMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "updateBeforeRender", at = @At("TAIL"), require = 0)
    private void ae2Wide$placeEaepButtonsAfterEaep(CallbackInfo ci) {
        if (getStyle().getTerminalStyle().getSlotsPerRow() <= 9) {
            return;
        }

        var processingPanel = modePanels.get(EncodingMode.PROCESSING);
        if (processingPanel == null) {
            return;
        }

        if (ae2Wide$eaepButtons == null) {
            ae2Wide$eaepButtons = Arrays.stream(processingPanel.getClass().getDeclaredFields())
                    .filter(field -> field.getType().getName()
                            .equals("com.extendedae_plus.client.gui.widgets.ScaledTextureButton"))
                    .peek(Field::trySetAccessible)
                    .toArray(Field[]::new);
        }

        int horizontalOffset;
        if (ae2Wide$isRewrittenUniversalTerminal()) {
            horizontalOffset = 169;
        } else {
            horizontalOffset = ae2Wide$isWirelessUniversalTerminal() ? 162 : 81;
        }
        for (var field : ae2Wide$eaepButtons) {
            try {
                if (field.get(processingPanel) instanceof AbstractWidget button) {
                    button.setX(button.getX() + horizontalOffset);
                }
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    @Unique
    private boolean ae2Wide$isWirelessUniversalTerminal() {
        if (!getClass().getName().equals("de.mari_023.ae2wtlib.wet.WETScreen")) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(menu.getClass().getMethod("isWUT").invoke(menu));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    @Unique
    private boolean ae2Wide$isRewrittenUniversalTerminal() {
        return getClass().getName()
                .equals("dev.codex.ae2widewireless.client.WideUniversalScreen");
    }
}
