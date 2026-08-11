package dev.codex.ae2widewireless.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.core.AEConfig;

import de.mari_023.ae2wtlib.api.TextConstants;
import de.mari_023.ae2wtlib.api.gui.Icon;
import de.mari_023.ae2wtlib.api.gui.IconButton;
import de.mari_023.ae2wtlib.api.gui.ScrollingUpgradesPanel;
import de.mari_023.ae2wtlib.api.terminal.IUniversalTerminalCapable;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.ArmorSlot;
import de.mari_023.ae2wtlib.wct.PlayerEntityWidget;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

public final class WideUniversalScreen extends PatternEncodingTermScreen<WideUniversalMenu>
        implements IUniversalTerminalCapable {
    private static final Blitter MANUAL_CRAFTING_BACKGROUND = Blitter.texture("guis/pattern_modes.png")
            .src(0, 0, 124, 66);

    private final IconButton magnetCardMenuButton;
    private final ScrollingUpgradesPanel upgradesPanel;

    public WideUniversalScreen(WideUniversalMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        var clearGrid = new ActionButton(ActionItems.S_STASH, button -> menu.clearManualCraftingGrid());
        clearGrid.setHalfSize(true);
        clearGrid.setDisableBackground(true);
        widgets.add("clearManualCraftingGrid", clearGrid);

        var clearToPlayer = new ActionButton(
                ActionItems.S_STASH_TO_PLAYER_INV,
                button -> menu.clearManualToPlayerInventory());
        clearToPlayer.setHalfSize(true);
        clearToPlayer.setDisableBackground(true);
        widgets.add("clearManualToPlayer", clearToPlayer);

        magnetCardMenuButton = new CompactMagnetButton(button -> menu.openMagnetMenu());
        magnetCardMenuButton.setMessage(TextConstants.MAGNET_FILTER);
        widgets.add("magnetCardMenuButton", magnetCardMenuButton);

        var wirelessTerminalSettingsButton = IconButton.withAE2Background(
                button -> switchToScreen(new WideUniversalSettingsScreen(this)),
                Icon.TERMINAL_SETTINGS);
        wirelessTerminalSettingsButton.setMessage(TextConstants.TERMINAL_SETTINGS);
        widgets.add("wirelessTerminalSettingsButton", wirelessTerminalSettingsButton);

        widgets.add("jeiTransferTarget", new RecipeTransferTargetButton(menu));

        var trashButton = IconButton.withAE2Background(
                button -> menu.openTrashMenu(), Icon.TRASH);
        trashButton.setMessage(TextConstants.TRASH);
        widgets.add("trashButton", trashButton);

        // AE2WTLib's selector switches only between terminal cards installed
        // in the universal terminal. The two work areas below remain part of
        // this single combined screen and are not part of this switch.
        var terminalSelector = cycleTerminalButton();
        addToLeftToolbar(terminalSelector);

        widgets.add("playerPreview", new PlayerEntityWidget(playerInventory.player));

        upgradesPanel = addUpgradePanel(widgets, menu);
    }

    @Override
    public void init() {
        super.init();
        upgradesPanel.setMaxRows(Math.max(2, getVisibleRows()));
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY,
            int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        MANUAL_CRAFTING_BACKGROUND
                .dest(offsetX + 8, offsetY + imageHeight - 165)
                .blit(guiGraphics);

        // Match AE2WTLib's wireless crafting terminal module: the four armor
        // slots touch the player viewport and the offhand aligns with boots.
        guiGraphics.fill(offsetX + 25, offsetY + imageHeight - 85,
                offsetX + 72, offsetY + imageHeight - 9, 0xFF000000);
        drawSlotBackground(guiGraphics, offsetX + 7, offsetY + imageHeight - 85);
        drawSlotBackground(guiGraphics, offsetX + 7, offsetY + imageHeight - 66);
        drawSlotBackground(guiGraphics, offsetX + 7, offsetY + imageHeight - 46);
        drawSlotBackground(guiGraphics, offsetX + 7, offsetY + imageHeight - 27);
        drawSlotBackground(guiGraphics, offsetX + 72, offsetY + imageHeight - 27);
    }

    private static void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF696D88);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF9A9FB4);
        guiGraphics.fill(x + 1, y + 2, x + 17, y + 17, 0xFFADB0C4);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        magnetCardMenuButton.setVisibility(switch (getMenu().getMagnetMode()) {
            case INVALID, NO_CARD -> false;
            case OFF, PICKUP_ME, PICKUP_INVENTORY, PICKUP_ME_NO_MAGNET -> true;
        });
    }

    @Override
    public void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof ArmorSlot armorSlot) {
            if (armorSlot.getItem().isEmpty() && armorSlot.isSlotEnabled()) {
                armorSlot.icon().getBlitter()
                        .dest(armorSlot.x, armorSlot.y)
                        .opacity(armorSlot.getOpacityOfIcon())
                        .blit(guiGraphics);
            }
        }
        super.renderSlot(guiGraphics, slot);
    }

    @Override
    public void onClose() {
        if (AEConfig.instance().isClearGridOnClose()) {
            getMenu().clearManualCraftingGrid();
        }
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        return handled || checkForTerminalKeys(keyCode, scanCode);
    }

    @Override
    public WTMenuHost getHost() {
        return (WTMenuHost) getMenu().getHost();
    }
}
