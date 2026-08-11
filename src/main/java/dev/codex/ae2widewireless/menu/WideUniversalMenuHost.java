package dev.codex.ae2widewireless.menu;

import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.inventories.InternalInventory;
import appeng.helpers.IPatternTerminalLogicHost;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.parts.encoding.PatternEncodingLogic;

import de.mari_023.ae2wtlib.api.AE2wtlibComponents;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wct.WCTMenuHost;

public final class WideUniversalMenuHost extends WCTMenuHost
        implements IViewCellStorage, IPatternTerminalMenuHost, IPatternTerminalLogicHost {
    private final PatternEncodingLogic patternLogic = new PatternEncodingLogic(this);

    public static WideUniversalMenuHost from(WTMenuHost host) {
        if (host instanceof WideUniversalMenuHost wideHost) {
            return wideHost;
        }

        var item = host.getItem();
        var locator = host.getLocator();
        return new WideUniversalMenuHost(
                item,
                host.getPlayer(),
                locator,
                (returningPlayer, submenu) -> item.tryOpen(returningPlayer, locator, true));
    }

    public WideUniversalMenuHost(ItemWT item, Player player, ItemMenuHostLocator locator,
            BiConsumer<Player, ISubMenu> returnToMainMenu) {
        super(item, player, locator, returnToMainMenu);
        patternLogic.readFromNBT(
                getItemStack().getOrDefault(AE2wtlibComponents.PATTERN_ENCODING_LOGIC, new CompoundTag()),
                player.registryAccess());
    }

    @Override
    public InternalInventory getViewCellStorage() {
        return super.getViewCellStorage();
    }

    @Override
    public PatternEncodingLogic getLogic() {
        return patternLogic;
    }

    @Override
    public Level getLevel() {
        return getPlayer().level();
    }

    @Override
    public void markForSave() {
        var tag = getItemStack().getOrDefault(
                AE2wtlibComponents.PATTERN_ENCODING_LOGIC,
                new CompoundTag());
        patternLogic.writeToNBT(tag, getPlayer().registryAccess());
        getItemStack().set(AE2wtlibComponents.PATTERN_ENCODING_LOGIC, tag);
    }
}
