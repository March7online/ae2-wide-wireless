package dev.codex.ae2widewireless.mixin;

import appeng.client.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * AE2 Lightning Tech's Tianshu panels use the same composite-widget bounds as
 * AE2's native encoding panels. The wide style positions that widget at x=73,
 * so AE2's native +8 draw offset lands on the centered x=81 work-area origin.
 */
@Pseudo
@Mixin(targets = {
        "com.moakiee.ae2lt.client.TianshuCraftingEncodingPanel",
        "com.moakiee.ae2lt.client.TianshuProcessingEncodingPanel",
        "com.moakiee.ae2lt.client.TianshuSmithingTableEncodingPanel",
        "com.moakiee.ae2lt.client.TianshuStonecuttingEncodingPanel"
}, remap = false)
public abstract class TianshuEncodingPanelMixin {
    @ModifyConstant(
            method = "drawBackgroundLayer",
            constant = @Constant(intValue = 8),
            require = 1,
            remap = false)
    private int ae2Wide$centerTianshuPanelBackground(
            int original, GuiGraphics graphics, Rect2i bounds, Point mouse) {
        return original;
    }
}
