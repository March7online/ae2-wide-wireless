package dev.codex.ae2widewireless.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.client.gui.AEBaseScreen;

import mezz.jei.api.ingredients.ITypedIngredient;

import dev.codex.ae2widewireless.client.WideUniversalScreen;

/** Prevents AE2 JEI Integration's broad AEBaseScreen handler from also highlighting the pattern slots. */
@Pseudo
@Mixin(targets = "tamaized.ae2jeiintegration.integration.modules.jei.GhostIngredientHandler",
        remap = false)
public abstract class Ae2JeiGhostIngredientHandlerMixin {
    @Inject(method = "getTargetsTyped", at = @At("HEAD"), cancellable = true, require = 0)
    private void ae2Wide$useTargetAwareHandler(AEBaseScreen<?> screen,
            ITypedIngredient<?> ingredient,
            boolean doStart,
            CallbackInfoReturnable<List<?>> cir) {
        if (screen instanceof WideUniversalScreen) {
            cir.setReturnValue(List.of());
        }
    }
}
