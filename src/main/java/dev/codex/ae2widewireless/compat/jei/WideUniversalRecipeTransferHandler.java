package dev.codex.ae2widewireless.compat.jei;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import appeng.integration.modules.itemlists.EncodingHelper;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;

import dev.codex.ae2widewireless.WideWirelessMod;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

public final class WideUniversalRecipeTransferHandler
        implements IUniversalRecipeTransferHandler<WideUniversalMenu> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public WideUniversalRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends WideUniversalMenu> getContainerClass() {
        return WideUniversalMenu.class;
    }

    @Override
    public Optional<MenuType<WideUniversalMenu>> getMenuType() {
        return Optional.of(WideWirelessMod.WIDE_UNIVERSAL_MENU);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IRecipeTransferError transferRecipe(WideUniversalMenu menu,
            Object recipeObject,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {
        RecipeHolder<?> holder = recipeObject instanceof RecipeHolder<?> recipeHolder
                ? recipeHolder
                : null;
        Recipe<?> recipe = holder != null
                ? holder.value()
                : recipeObject instanceof Recipe<?> directRecipe ? directRecipe : null;

        if (menu.isRecipeTransferToManualGrid()) {
            if (holder != null && recipe instanceof CraftingRecipe craftingRecipe) {
                if (doTransfer) {
                    WideJeiTransfers.fillManualCraftingGrid(
                            (RecipeHolder<CraftingRecipe>) holder, recipeSlots);
                }
                return null;
            }
            return transferHelper.createUserErrorWithTooltip(Component.translatable(
                    "gui.ae2_wide_wireless.jei_target.manual_only_crafting"));
        }

        if (!doTransfer) {
            return null;
        }

        var inputs = WideJeiTransfers.collectAlternatives(
                recipeSlots, RecipeIngredientRole.INPUT);
        if (EncodingHelper.isSupportedCraftingRecipe(recipe)) {
            EncodingHelper.encodeCraftingRecipe(menu, holder, inputs, stack -> true);
        } else {
            EncodingHelper.encodeProcessingRecipe(
                    menu,
                    inputs,
                    WideJeiTransfers.collectResults(recipeSlots));
        }
        return null;
    }
}
