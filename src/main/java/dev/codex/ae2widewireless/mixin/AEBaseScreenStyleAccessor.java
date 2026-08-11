package dev.codex.ae2widewireless.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;

@Mixin(AEBaseScreen.class)
public interface AEBaseScreenStyleAccessor {
    @Accessor("style")
    @Mutable
    void ae2Wide$setScreenStyle(ScreenStyle style);

    @Invoker("positionSlots")
    void ae2Wide$repositionSlots();
}
