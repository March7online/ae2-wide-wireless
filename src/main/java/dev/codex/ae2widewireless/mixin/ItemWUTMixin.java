package dev.codex.ae2widewireless.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import dev.codex.ae2widewireless.WideWirelessMod;

@Mixin(ItemWUT.class)
public abstract class ItemWUTMixin {
    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    private void ae2Wide$openCombinedTerminal(Player player, ItemMenuHostLocator locator,
            boolean returningFromSubmenu, CallbackInfoReturnable<Boolean> cir) {
        if (WideWirelessMod.shouldOpenWideUniversal(locator.locateItem(player))
                && MenuOpener.open(
                    WideWirelessMod.WIDE_UNIVERSAL_MENU,
                    player,
                    locator,
                    returningFromSubmenu)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getMenuType", at = @At("HEAD"), cancellable = true)
    private void ae2Wide$getCombinedMenuType(ItemMenuHostLocator locator, Player player,
            CallbackInfoReturnable<MenuType<?>> cir) {
        if (WideWirelessMod.shouldOpenWideUniversal(locator.locateItem(player))) {
            cir.setReturnValue(WideWirelessMod.WIDE_UNIVERSAL_MENU);
        }
    }
}
