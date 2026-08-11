package dev.codex.ae2widewireless.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import appeng.client.gui.style.StyleManager;
import dev.codex.ae2widewireless.TerminalWidthState;

/** Selects the private Tianshu style before AE2 initializes either style field. */
@Pseudo
@Mixin(targets = "com.moakiee.ae2lt.client.TianshuPatternEncodingTermScreen", remap = false)
public abstract class TianshuInitialStyleMixin {
    private static final String WIRELESS_MENU =
            "com.moakiee.ae2lt.menu.TianshuWirelessPatternEncodingTermMenu";

    @ModifyArgs(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/client/gui/me/common/MEStorageScreen;<init>(Lappeng/menu/me/common/MEStorageMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;Lappeng/client/gui/style/ScreenStyle;)V"),
            require = 1)
    private static void ae2Wide$selectInitialStyle(Args args) {
        Object menu = args.get(0);
        boolean wireless = WIRELESS_MENU.equals(menu.getClass().getName());
        args.set(3, StyleManager.loadStyleDoc(TerminalWidthState.getTianshuStylePath(wireless)));
    }
}
