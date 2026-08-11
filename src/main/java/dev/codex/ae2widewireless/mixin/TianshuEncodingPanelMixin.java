package dev.codex.ae2widewireless.mixin;

import appeng.client.Point;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * AE2 Lightning Tech's Tianshu panels draw their background from the terminal
 * origin and use a fixed x=8 offset instead of the composite widget position.
 * The wide style moves that background to the centered work area.
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
        return bounds.getWidth() > 195 ? 89 : original;
    }
}
