package dev.codex.ae2widewireless.client;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.widgets.AECheckbox;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.SlotSemantics;

import de.mari_023.ae2wtlib.AE2wtlibAdditionalComponents;
import de.mari_023.ae2wtlib.api.AE2wtlibComponents;
import de.mari_023.ae2wtlib.api.TextConstants;
import de.mari_023.ae2wtlib.networking.TerminalSettingsPacket;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetHandler;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMode;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

/**
 * AE2WTLib's settings screen is typed specifically to {@code WCTScreen} and
 * {@code WCTMenu}. The rewritten universal terminal uses its own menu, so this
 * adapter keeps the same style, components and packet while returning to the
 * rewritten parent screen.
 */
public final class WideUniversalSettingsScreen
        extends AESubScreen<WideUniversalMenu, WideUniversalScreen> {
    private final AECheckbox pickBlock;
    private final AECheckbox craftIfMissing;
    private final AECheckbox restock;
    private final AECheckbox magnet;
    private final AECheckbox pickupToME;

    public WideUniversalSettingsScreen(WideUniversalScreen parent) {
        super(parent, "/screens/wtlib/wireless_terminal_settings.json");

        pickBlock = widgets.addCheckbox(
                "pickBlock", TextConstants.PICK_BLOCK, this::changePickBlockVisibility);
        craftIfMissing = widgets.addCheckbox(
                "craftIfMissing", TextConstants.CRAFT_IF_MISSING, this::save);
        restock = widgets.addCheckbox("restock", TextConstants.RESTOCK, this::save);
        magnet = widgets.addCheckbox("magnet", TextConstants.MAGNET, this::save);
        pickupToME = widgets.addCheckbox("pickupToME", TextConstants.PICKUP_TO_ME, this::save);

        widgets.add("back", new TabButton(
                appeng.client.gui.Icon.BACK,
                getParent().getHost().getItemStack().getHoverName(),
                button -> returnToParent()));

        var terminalStack = terminalStack();
        pickBlock.setSelected(terminalStack.getOrDefault(AE2wtlibComponents.PICK_BLOCK, false));
        craftIfMissing.setSelected(
                terminalStack.getOrDefault(AE2wtlibComponents.CRAFT_IF_MISSING, false));
        craftIfMissing.active = pickBlock.isSelected();
        restock.setSelected(terminalStack.getOrDefault(AE2wtlibComponents.RESTOCK, false));
        magnet.setSelected(terminalStack
                .getOrDefault(AE2wtlibAdditionalComponents.MAGNET_SETTINGS, MagnetMode.OFF)
                .magnet());
        pickupToME.setSelected(terminalStack
                .getOrDefault(AE2wtlibAdditionalComponents.MAGNET_SETTINGS, MagnetMode.OFF)
                .pickupToME());

        if (MagnetHandler.getMagnetMode(terminalStack) == MagnetMode.NO_CARD) {
            magnet.active = false;
            pickupToME.active = false;
        }
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.TOOLBOX, true);
    }

    private ItemStack terminalStack() {
        return getParent().getHost().getItemStack();
    }

    private void changePickBlockVisibility() {
        craftIfMissing.active = pickBlock.isSelected();
        save();
    }

    private void save() {
        var locator = getParent().getHost().getLocator();
        if (locator == null) {
            return;
        }

        PacketDistributor.sendToServer(new TerminalSettingsPacket(
                locator,
                pickBlock.isSelected(),
                restock.isSelected(),
                magnet.isSelected(),
                pickupToME.isSelected(),
                craftIfMissing.isSelected()));
    }
}
