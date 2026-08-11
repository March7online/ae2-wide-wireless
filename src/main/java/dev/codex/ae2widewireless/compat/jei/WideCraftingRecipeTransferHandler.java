package dev.codex.ae2widewireless.compat.jei;

import java.util.Optional;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.core.localization.ItemModText;
import appeng.integration.modules.itemlists.EncodingHelper;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import dev.codex.ae2widewireless.WideWirelessMod;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

public final class WideCraftingRecipeTransferHandler
        implements IRecipeTransferHandler<WideUniversalMenu, RecipeHolder<CraftingRecipe>> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public WideCraftingRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<WideUniversalMenu> getContainerClass() {
        return WideUniversalMenu.class;
    }

    @Override
    public Optional<MenuType<WideUniversalMenu>> getMenuType() {
        return Optional.of(WideWirelessMod.WIDE_UNIVERSAL_MENU);
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public IRecipeTransferError transferRecipe(WideUniversalMenu menu,
            RecipeHolder<CraftingRecipe> recipeHolder,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer) {
        var recipe = recipeHolder.value();
        if (recipe.getType() != RecipeType.CRAFTING) {
            return transferHelper.createInternalError();
        }
        if (recipe.getIngredients().isEmpty()) {
            return transferHelper.createUserErrorWithTooltip(ItemModText.INCOMPATIBLE_RECIPE.text());
        }
        if (!recipe.canCraftInDimensions(3, 3)) {
            return transferHelper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        if (doTransfer) {
            if (menu.isRecipeTransferToManualGrid()) {
                WideJeiTransfers.fillManualCraftingGrid(recipeHolder, recipeSlots);
            } else {
                EncodingHelper.encodeCraftingRecipe(
                        menu,
                        recipeHolder,
                        WideJeiTransfers.collectAlternatives(
                                recipeSlots, mezz.jei.api.recipe.RecipeIngredientRole.INPUT),
                        stack -> true);
            }
        }
        return null;
    }
}
