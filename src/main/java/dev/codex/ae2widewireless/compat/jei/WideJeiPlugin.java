package dev.codex.ae2widewireless.compat.jei;

import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;

import dev.codex.ae2widewireless.WideWirelessMod;
import dev.codex.ae2widewireless.client.WideUniversalScreen;

@JeiPlugin
public final class WideJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = WideWirelessMod.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(WideUniversalScreen.class,
                new IGuiContainerHandler<>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(WideUniversalScreen screen) {
                        return screen.getExclusionZones();
                    }
                });
        registration.addGhostIngredientHandler(
                WideUniversalScreen.class,
                new WideUniversalGhostIngredientHandler());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        var helper = registration.getTransferHelper();
        registration.addRecipeTransferHandler(
                new WideCraftingRecipeTransferHandler(helper),
                RecipeTypes.CRAFTING);
        registration.addUniversalRecipeTransferHandler(
                new WideUniversalRecipeTransferHandler(helper));
    }
}
