package dev.codex.ae2widewireless.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.helpers.WirelessTerminalMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;

import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import dev.codex.ae2widewireless.WideWirelessMod;
import dev.codex.ae2widewireless.menu.WideUniversalMenuHost;

@Mixin(ItemWT.class)
public abstract class ItemWTMenuHostMixin {
    @Inject(
            method = "getMenuHost(Lnet/minecraft/world/entity/player/Player;Lappeng/menu/locator/ItemMenuHostLocator;Lnet/minecraft/world/phys/BlockHitResult;)Lappeng/helpers/WirelessTerminalMenuHost;",
            at = @At("HEAD"),
            cancellable = true)
    private void ae2Wide$createCombinedMenuHost(Player player, ItemMenuHostLocator locator,
            BlockHitResult hitResult,
            CallbackInfoReturnable<WirelessTerminalMenuHost<?>> cir) {
        if (!((Object) this instanceof ItemWUT)
                || !WideWirelessMod.supportsCombinedLayout(locator.locateItem(player))) {
            return;
        }

        var item = (ItemWT) (Object) this;
        cir.setReturnValue(new WideUniversalMenuHost(
                item,
                player,
                locator,
                (returningPlayer, submenu) -> item.tryOpen(returningPlayer, locator, true)));
    }
}
