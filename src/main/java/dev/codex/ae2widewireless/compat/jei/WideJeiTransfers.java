package dev.codex.ae2widewireless.compat.jei;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.stacks.GenericStack;
import appeng.core.network.serverbound.FillCraftingGridFromRecipePacket;
import appeng.util.CraftingRecipeUtil;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;

final class WideJeiTransfers {
    private WideJeiTransfers() {
    }

    static void fillManualCraftingGrid(RecipeHolder<CraftingRecipe> recipeHolder,
            IRecipeSlotsView recipeSlots) {
        var templates = NonNullList.withSize(9, ItemStack.EMPTY);
        var ingredients = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipeHolder.value());
        var inputViews = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);

        for (int slot = 0; slot < ingredients.size() && slot < templates.size(); slot++) {
            ItemStack displayed = getDisplayedItem(inputViews, slot);
            if (!displayed.isEmpty() && ingredients.get(slot).test(displayed)) {
                templates.set(slot, displayed.copyWithCount(1));
                continue;
            }

            var candidates = ingredients.get(slot).getItems();
            if (candidates.length > 0 && !candidates[0].isEmpty()) {
                templates.set(slot, candidates[0].copyWithCount(1));
            }
        }

        PacketDistributor.sendToServer(new FillCraftingGridFromRecipePacket(
                recipeHolder.id(), templates, Screen.hasControlDown()));
    }

    private static ItemStack getDisplayedItem(List<IRecipeSlotView> inputViews, int slot) {
        if (slot < 0 || slot >= inputViews.size()) {
            return ItemStack.EMPTY;
        }
        return inputViews.get(slot).getItemStacks()
                .filter(stack -> !stack.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    static List<List<GenericStack>> collectAlternatives(IRecipeSlotsView recipeSlots,
            RecipeIngredientRole role) {
        var result = new ArrayList<List<GenericStack>>();
        for (var slot : recipeSlots.getSlotViews(role)) {
            result.add(slot.getAllIngredients()
                    .map(WideJeiTransfers::toGenericStack)
                    .filter(java.util.Objects::nonNull)
                    .toList());
        }
        return result;
    }

    static List<GenericStack> collectResults(IRecipeSlotsView recipeSlots) {
        var result = new ArrayList<GenericStack>();
        for (var alternatives : collectAlternatives(recipeSlots, RecipeIngredientRole.OUTPUT)) {
            if (!alternatives.isEmpty()) {
                result.add(alternatives.getFirst());
            }
        }
        return result;
    }

    @Nullable
    static GenericStack toGenericStack(ITypedIngredient<?> ingredient) {
        Object raw = ingredient.getIngredient();
        if (raw instanceof ItemStack stack && !stack.isEmpty()) {
            return GenericStack.fromItemStack(stack.copy());
        }
        if (raw instanceof FluidStack fluid && !fluid.isEmpty()) {
            return GenericStack.fromFluidStack(fluid.copy());
        }
        if (raw instanceof SizedFluidIngredient sized) {
            for (var fluid : sized.getFluids()) {
                if (!fluid.isEmpty()) {
                    var copy = fluid.copy();
                    copy.setAmount(Math.max(1, sized.amount()));
                    return GenericStack.fromFluidStack(copy);
                }
            }
        }
        return null;
    }
}
