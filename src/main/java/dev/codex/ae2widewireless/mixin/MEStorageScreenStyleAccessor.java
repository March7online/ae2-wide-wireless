package dev.codex.ae2widewireless.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.TerminalStyle;

@Mixin(MEStorageScreen.class)
public interface MEStorageScreenStyleAccessor {
    @Accessor("style")
    @Mutable
    void ae2Wide$setTerminalStyle(TerminalStyle style);
}
