package dev.codex.ae2widewireless.mixin;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.api.terminal.WUTHandler;
import de.mari_023.ae2wtlib.networking.SelectTerminalPacket;
import dev.codex.ae2widewireless.WideWirelessMod;

/**
 * Routes only terminal-selector transitions back to the dual-layout screen.
 * Normal wireless-terminal opening remains entirely on AE2WTLib's native
 * ItemWUT/WUTHandler path.
 */
@Mixin(SelectTerminalPacket.class)
public abstract class SelectTerminalPacketMixin {
    @Redirect(
            method = "processPacketData",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/mari_023/ae2wtlib/api/terminal/WUTHandler;open(Lnet/minecraft/world/entity/player/Player;Lappeng/menu/locator/ItemMenuHostLocator;Z)Z"))
    private boolean ae2Wide$routeSelectedTerminal(Player player, ItemMenuHostLocator locator,
            boolean returningFromSubmenu) {
        if (WideWirelessMod.shouldOpenWideUniversal(locator.locateItem(player))
                && MenuOpener.open(
                    WideWirelessMod.WIDE_UNIVERSAL_MENU,
                    player,
                    locator,
                    returningFromSubmenu)) {
            return true;
        }

        return WUTHandler.open(player, locator, returningFromSubmenu);
    }
}
