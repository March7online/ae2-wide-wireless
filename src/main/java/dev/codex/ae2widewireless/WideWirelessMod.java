package dev.codex.ae2widewireless;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import appeng.init.client.InitScreens;
import appeng.menu.implementations.MenuTypeBuilder;

import de.mari_023.ae2wtlib.api.registration.WTDefinition;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import de.mari_023.ae2wtlib.api.terminal.WTMenuHost;
import dev.codex.ae2widewireless.client.WideUniversalScreen;
import dev.codex.ae2widewireless.menu.WideUniversalMenu;

@Mod(WideWirelessMod.MOD_ID)
public final class WideWirelessMod {
    public static final String MOD_ID = "ae2_wide_wireless";
    public static final ResourceLocation WIDE_UNIVERSAL_ID = id("wide_universal_terminal");
    public static final MenuType<WideUniversalMenu> WIDE_UNIVERSAL_MENU = MenuTypeBuilder
            .create(WideUniversalMenu::new, WTMenuHost.class)
            .buildUnregistered(WIDE_UNIVERSAL_ID);

    public WideWirelessMod(IEventBus modEventBus) {
        WideSlotSemantics.init();
        modEventBus.addListener(WideWirelessMod::registerMenus);
    }

    private static void registerMenus(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.MENU)) {
            Registry.register(BuiltInRegistries.MENU, WIDE_UNIVERSAL_ID, WIDE_UNIVERSAL_MENU);
        }
    }

    public static boolean supportsCombinedLayout(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemWUT)) {
            return false;
        }

        var crafting = WTDefinition.of("crafting");
        var patternEncoding = WTDefinition.of("pattern_encoding");
        return stack.get(crafting.componentType()) != null
                && stack.get(patternEncoding.componentType()) != null;
    }

    /**
     * Returns whether the currently selected wireless terminal should open the
     * replacement dual-layout screen. Other wireless terminals, such as the
     * request terminal, remain on their native screen.
     */
    public static boolean shouldOpenWideUniversal(ItemStack stack) {
        if (!supportsCombinedLayout(stack)) {
            return false;
        }

        var current = WTDefinition.ofOrNull(stack);
        if (current == null) {
            return false;
        }

        return "crafting".equals(current.terminalName())
                || "pattern_encoding".equals(current.terminalName());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @EventBusSubscriber(
            modid = MOD_ID,
            value = Dist.CLIENT,
            bus = EventBusSubscriber.Bus.MOD)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            InitScreens.register(
                    event,
                    WIDE_UNIVERSAL_MENU,
                    WideUniversalScreen::new,
                    "/screens/ae2_wide_wireless/wide_universal_terminal.json");
        }
    }
}
