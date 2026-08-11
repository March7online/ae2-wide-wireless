package dev.codex.ae2widewireless.mixin;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.gui.AEBaseScreen;
import appeng.menu.me.common.MEStorageMenu;
import appeng.client.gui.style.ScreenStyle;
import dev.codex.ae2widewireless.WidgetContainerStyleBridge;

/**
 * Tianshu adds its mode tabs and encoding panels in the subclass constructor,
 * after AE2's base screen has already performed the normal widget placement.
 * Reapply the resolved wide style at the end of init so those late controls
 * and their composite panels use the same +81 layout shift as the slots.
 */
@Pseudo
@Mixin(targets = {
        "com.moakiee.ae2lt.client.TianshuPatternEncodingTermScreen",
        "com.moakiee.ae2lt.client.TianshuWirelessPatternEncodingTermScreen"
}, remap = false)
public abstract class TianshuPatternEncodingScreenMixin extends AEBaseScreen<MEStorageMenu> {
    protected TianshuPatternEncodingScreenMixin(MEStorageMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ae2Wide$reflowLateTianshuWidgets(CallbackInfo ci) {
        if (getStyle().getTerminalStyle().getSlotsPerRow() <= 9) {
            return;
        }

        ((WidgetContainerStyleBridge) (Object) widgets).ae2Wide$setStyle(
                getStyle(),
                new Rect2i(getGuiLeft(), getGuiTop(), imageWidth, imageHeight),
                new Rect2i(0, 0, imageWidth, imageHeight));
    }
}
