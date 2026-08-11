package dev.codex.ae2widewireless.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.helpers.ICraftingGridMenu;
import appeng.helpers.InventoryAction;
import appeng.me.storage.LinkStatusRespectingInventory;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.core.network.serverbound.InventoryActionPacket;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.PlayerInternalInventory;

import de.mari_023.ae2wtlib.api.gui.AE2wtlibSlotSemantics;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.ArmorSlot;
import de.mari_023.ae2wtlib.wct.TrashMenu;
import de.mari_023.ae2wtlib.wct.WCTMenu;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetHandler;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMenu;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMode;
import dev.codex.ae2widewireless.WideSlotSemantics;
import dev.codex.ae2widewireless.WideWirelessMod;

public final class WideUniversalMenu extends PatternEncodingTermMenu implements ICraftingGridMenu {
    private static final String ACTION_CLEAR_MANUAL_TO_PLAYER = "clearManualToPlayer";
    private static boolean recipeTransferToManualGrid = true;

    private final WideUniversalMenuHost menuHost;
    private final CraftingMatrixSlot[] manualCraftingSlots = new CraftingMatrixSlot[9];
    private final CraftingTermSlot manualOutputSlot;
    private CraftingInput lastManualInput;
    private RecipeHolder<CraftingRecipe> currentManualRecipe;

    public WideUniversalMenu(int id, Inventory playerInventory, WTMenuHost host) {
        this(id, playerInventory, WideUniversalMenuHost.from(host));
    }

    public WideUniversalMenu(int id, Inventory playerInventory, WideUniversalMenuHost host) {
        super(WideWirelessMod.WIDE_UNIVERSAL_MENU, id, playerInventory, host, false);
        menuHost = host;

        var craftingGrid = host.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
        for (int i = 0; i < manualCraftingSlots.length; i++) {
            addSlot(manualCraftingSlots[i] = new CraftingMatrixSlot(this, craftingGrid, i),
                    WideSlotSemantics.MANUAL_CRAFTING_GRID);
        }

        var linkedStorage = new LinkStatusRespectingInventory(host.getInventory(), this::getLinkStatus);
        manualOutputSlot = new CraftingTermSlot(
                getPlayerInventory().player,
                getActionSource(),
                energySource,
                linkedStorage,
                craftingGrid,
                craftingGrid,
                this);
        addSlot(manualOutputSlot, WideSlotSemantics.MANUAL_CRAFTING_RESULT);

        var singularityInventory = host.getSubInventory(WTMenuHost.INV_SINGULARITY);
        if (singularityInventory != null) {
            addSlot(new RestrictedInputSlot(
                    RestrictedInputSlot.PlacableItemType.QE_SINGULARITY,
                    singularityInventory,
                    0), AE2wtlibSlotSemantics.SINGULARITY);
        }

        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.HEAD),
                AE2wtlibSlotSemantics.HELMET);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.CHEST),
                AE2wtlibSlotSemantics.CHESTPLATE);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.LEGS),
                AE2wtlibSlotSemantics.LEGGINGS);
        addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.FEET),
                AE2wtlibSlotSemantics.BOOTS);
        if (Integer.valueOf(Inventory.SLOT_OFFHAND).equals(menuHost.getPlayerInventorySlot())) {
            addSlot(new ArmorSlot.DisabledOffhandSlot(getPlayerInventory()),
                    AE2wtlibSlotSemantics.OFFHAND);
        } else {
            addSlot(new ArmorSlot(getPlayerInventory(), ArmorSlot.Armor.OFFHAND),
                    AE2wtlibSlotSemantics.OFFHAND);
        }

        registerClientAction(ACTION_CLEAR_MANUAL_TO_PLAYER, this::clearManualToPlayerInventory);
        registerClientAction(WCTMenu.MAGNET_MENU, this::openMagnetMenu);
        registerClientAction(WCTMenu.TRASH_MENU, this::openTrashMenu);
        createPlayerInventorySlots(playerInventory);
        updateManualRecipeAndOutput(true);
    }

    @Override
    public IGridNode getGridNode() {
        return menuHost.getActionableNode();
    }

    @Override
    public IEnergySource getEnergySource() {
        return energySource;
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return menuHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
    }

    @Override
    public void startAutoCrafting(List<AutoCraftEntry> toCraft) {
        CraftConfirmMenu.openWithCraftingList(getActionHost(), (ServerPlayer) getPlayer(), getLocator(), toCraft);
    }

    @Override
    protected boolean canSlotsBeHidden(appeng.menu.SlotSemantic semantic) {
        return semantic == AE2wtlibSlotSemantics.OFFHAND
                || semantic == AE2wtlibSlotSemantics.HELMET
                || semantic == AE2wtlibSlotSemantics.CHESTPLATE
                || semantic == AE2wtlibSlotSemantics.LEGGINGS
                || semantic == AE2wtlibSlotSemantics.BOOTS;
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
        updateManualRecipeAndOutput(false);
    }

    private void updateManualRecipeAndOutput(boolean forceUpdate) {
        if (manualCraftingSlots[0] == null) {
            return;
        }

        var items = new ArrayList<ItemStack>(manualCraftingSlots.length);
        for (var slot : manualCraftingSlots) {
            items.add(slot.getItem().copy());
        }
        var input = CraftingInput.of(3, 3, items);
        if (!forceUpdate && Objects.equals(lastManualInput, input)) {
            return;
        }

        var level = getPlayer().level();
        currentManualRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, level)
                .orElse(null);
        lastManualInput = input;
        manualOutputSlot.set(currentManualRecipe == null
                ? ItemStack.EMPTY
                : currentManualRecipe.value().assemble(input, level.registryAccess()));
    }

    public RecipeHolder<CraftingRecipe> getCurrentManualRecipe() {
        return currentManualRecipe;
    }

    /**
     * This is deliberately client-local: it only tells JEI which of the two
     * permanently visible work areas should receive the next recipe.
     */
    public boolean isRecipeTransferToManualGrid() {
        return recipeTransferToManualGrid;
    }

    public void toggleRecipeTransferTarget() {
        if (isClientSide()) {
            recipeTransferToManualGrid = !recipeTransferToManualGrid;
        }
    }

    public void clearManualCraftingGrid() {
        if (!isClientSide()) {
            return;
        }
        PacketDistributor.sendToServer(new InventoryActionPacket(
                InventoryAction.MOVE_REGION,
                manualCraftingSlots[0].index,
                0));
    }

    public void clearManualToPlayerInventory() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR_MANUAL_TO_PLAYER);
            return;
        }

        var craftingGrid = getCraftingMatrix();
        var playerInventory = new PlayerInternalInventory(getPlayerInventory());
        for (int i = 0; i < craftingGrid.size(); i++) {
            for (int emptyPass = 0; emptyPass < 2; emptyPass++) {
                boolean allowEmpty = emptyPass == 1;
                for (int j = 8; j >= 0; j--) {
                    if (playerInventory.getStackInSlot(j).isEmpty() == allowEmpty) {
                        craftingGrid.setItemDirect(i,
                                playerInventory.getSlotInv(j).addItems(craftingGrid.getStackInSlot(i)));
                    }
                }
                for (int j = 9; j < Inventory.INVENTORY_SIZE; j++) {
                    if (playerInventory.getStackInSlot(j).isEmpty() == allowEmpty) {
                        craftingGrid.setItemDirect(i,
                                playerInventory.getSlotInv(j).addItems(craftingGrid.getStackInSlot(i)));
                    }
                }
            }
        }
    }

    public MagnetMode getMagnetMode() {
        return MagnetHandler.getMagnetMode(menuHost.getItemStack());
    }

    /** Returns the exact locator used to open this universal terminal. */
    public ItemMenuHostLocator getTerminalLocator() {
        return menuHost.getLocator();
    }

    public void openMagnetMenu() {
        if (isClientSide()) {
            sendClientAction(WCTMenu.MAGNET_MENU);
            return;
        }
        MenuOpener.open(MagnetMenu.TYPE, getPlayer(), getLocator());
    }

    public void openTrashMenu() {
        if (isClientSide()) {
            sendClientAction(WCTMenu.TRASH_MENU);
            return;
        }
        MenuOpener.open(TrashMenu.TYPE, getPlayer(), getLocator());
    }
}
