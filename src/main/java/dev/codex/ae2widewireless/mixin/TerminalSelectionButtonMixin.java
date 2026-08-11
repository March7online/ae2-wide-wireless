package dev.codex.ae2widewireless.mixin;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.mari_023.ae2wtlib.api.registration.WTDefinition;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;

/**
 * The native selector lists every installed WUT card. A combined WUT has both
 * crafting and pattern-encoding cards, but both cards now open the same wide
 * universal screen, so the duplicate pattern-encoding entry is redundant.
 */
@Mixin(targets = "de.mari_023.ae2wtlib.api.terminal.TerminalSelectionButton")
public abstract class TerminalSelectionButtonMixin {
    @Shadow @Final private WTMenuHost host;
    @Shadow @Final private List<WTDefinition> terminals;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ae2Wide$collapseCombinedWirelessEntries(
            WTMenuHost host, Runnable storeState, CallbackInfo ci) {
        ItemStack stack = this.host.getItemStack();
        if (!(stack.getItem() instanceof ItemWUT)
                || !supportsCombinedLayout(stack)) {
            return;
        }

        terminals.removeIf(definition -> "pattern_encoding".equals(definition.terminalName()));
    }

    @Inject(method = "currentTooltip", at = @At("HEAD"), cancellable = true)
    private void ae2Wide$labelCombinedEntry(CallbackInfoReturnable<List<Component>> cir) {
        if (supportsCombinedLayout(this.host.getItemStack())) {
            cir.setReturnValue(List.of(Component.translatable(
                    "ae2_wide_wireless.terminal_selector.wireless_universal")));
        }
    }

    private static boolean supportsCombinedLayout(ItemStack stack) {
        var crafting = WTDefinition.of("crafting");
        var patternEncoding = WTDefinition.of("pattern_encoding");
        return stack.get(crafting.componentType()) != null
                && stack.get(patternEncoding.componentType()) != null;
    }
}
