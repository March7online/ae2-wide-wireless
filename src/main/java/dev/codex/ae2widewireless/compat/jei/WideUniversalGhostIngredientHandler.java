package dev.codex.ae2widewireless.compat.jei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.FakeSlot;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

import dev.codex.ae2widewireless.client.WideUniversalScreen;

/** Makes JEI's drag highlight follow the arrow-selected work area. */
final class WideUniversalGhostIngredientHandler
        implements IGhostIngredientHandler<WideUniversalScreen> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(WideUniversalScreen screen,
            ITypedIngredient<I> ingredient,
            boolean doStart) {
        ItemStack wrapped = wrap(ingredient);
        if (wrapped.isEmpty()) {
            return List.of();
        }

        boolean manualTarget = screen.getMenu().isRecipeTransferToManualGrid();
        List<Target<I>> targets = new ArrayList<>();
        for (var slot : screen.getMenu().slots) {
            if (!slot.isActive()) {
                continue;
            }

            if (manualTarget && slot instanceof CraftingMatrixSlot) {
                // JEI uses these target rectangles for the green drag overlay.
                // The recipe-transfer button remains responsible for pulling
                // real ingredients from the network into this grid.
                targets.add(new SlotTarget<>(screen, slot.x, slot.y, ignored -> {
                }));
            } else if (!manualTarget && slot instanceof FakeSlot fakeSlot
                    && fakeSlot.canSetFilterTo(wrapped)) {
                targets.add(new SlotTarget<>(screen, slot.x, slot.y,
                        ignored -> fakeSlot.setFilterTo(wrapped)));
            }
        }
        return targets;
    }

    private static ItemStack wrap(ITypedIngredient<?> ingredient) {
        Object raw = ingredient.getIngredient();
        if (raw instanceof ItemStack stack && !stack.isEmpty()) {
            return stack.copy();
        }
        var generic = WideJeiTransfers.toGenericStack(ingredient);
        return generic == null ? ItemStack.EMPTY : GenericStack.wrapInItemStack(generic);
    }

    @Override
    public void onComplete() {
    }

    private record SlotTarget<I>(Rect2i area, java.util.function.Consumer<I> action)
            implements Target<I> {
        SlotTarget(WideUniversalScreen screen, int x, int y,
                java.util.function.Consumer<I> action) {
            this(new Rect2i(screen.getGuiLeft() + x, screen.getGuiTop() + y, 16, 16), action);
        }

        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(I ingredient) {
            action.accept(ingredient);
        }
    }
}
