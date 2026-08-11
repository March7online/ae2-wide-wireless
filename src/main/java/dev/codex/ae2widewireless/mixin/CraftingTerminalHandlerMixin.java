package dev.codex.ae2widewireless.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

/**
 * AE2WTLib caches the first valid crafting terminal it finds. When several
 * wireless terminals are carried, that cache can still point at a different
 * terminal after the combined universal terminal is opened. Reset only the
 * native cache once for each newly opened combined menu so the library can
 * run its normal universal-terminal-first lookup again.
 */
@Mixin(CraftingTerminalHandler.class)
public abstract class CraftingTerminalHandlerMixin {
    @Shadow
    @Final
    public Player player;

    @Shadow
    protected abstract void invalidateCache();

    @Shadow
    private ItemMenuHostLocator locator;

    @Unique
    private AbstractContainerMenu ae2Wide$lastCombinedMenu;

    @Inject(method = "getLocator", at = @At("HEAD"))
    private void ae2Wide$refreshForCombinedMenu(
            CallbackInfoReturnable<ItemMenuHostLocator> cir) {
        var currentMenu = player.containerMenu;
        if (currentMenu instanceof WideUniversalMenu
                && currentMenu != ae2Wide$lastCombinedMenu) {
            ae2Wide$lastCombinedMenu = currentMenu;
            invalidateCache();
            locator = ((WideUniversalMenu) currentMenu).getTerminalLocator();
        }
    }
}
