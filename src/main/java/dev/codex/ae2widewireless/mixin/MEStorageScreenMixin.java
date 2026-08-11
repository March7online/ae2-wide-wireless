package dev.codex.ae2widewireless.mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.stacks.AEKeyType;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.items.CraftingTermScreen;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.IconButton;
import appeng.menu.me.common.MEStorageMenu;
import dev.codex.ae2widewireless.TypeFilterSwitchButton;
import dev.codex.ae2widewireless.TerminalScreenPolicy;
import dev.codex.ae2widewireless.TerminalWidthState;
import dev.codex.ae2widewireless.WidgetContainerStyleBridge;

@Mixin(MEStorageScreen.class)
public abstract class MEStorageScreenMixin extends AEBaseScreen<MEStorageMenu> {
    @Shadow
    @Final
    protected Repo repo;

    @Unique
    private TypeFilterSwitchButton ae2Wide$itemFilterButton;
    @Unique
    private TypeFilterSwitchButton ae2Wide$fluidFilterButton;
    @Unique
    private IconButton ae2Wide$widthButton;
    protected MEStorageScreenMixin(MEStorageMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ae2Wide$addDirectFilterButtons(MEStorageMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style, CallbackInfo ci) {
        ae2Wide$itemFilterButton = ae2Wide$createFilterButton(
                "gui.ae2_wide_wireless.item_filter",
                "gui.ae2_wide_wireless.item_filter.hint",
                AEKeyType.items());
        widgets.add("wideItemFilter", ae2Wide$itemFilterButton);

        ae2Wide$fluidFilterButton = ae2Wide$createFilterButton(
                "gui.ae2_wide_wireless.fluid_filter",
                "gui.ae2_wide_wireless.fluid_filter.hint",
                AEKeyType.fluids());
        widgets.add("wideFluidFilter", ae2Wide$fluidFilterButton);

        // The rewritten universal terminal keeps direct item/fluid filtering,
        // but its fixed dual workspace must not receive a width switch.
        if (ae2Wide$hasFixedWidth()) {
            return;
        }

        ae2Wide$widthButton = new IconButton(button -> ae2Wide$toggleWidth()) {
            @Override
            protected Icon getIcon() {
                return null;
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                String label = TerminalWidthState.isWide() ? "\u5bbd" : "\u7a84";
                int textX = getX() + (16 - MEStorageScreenMixin.this.font.width(label)) / 2;
                int textY = getY() + 5 + (isHovered() ? 1 : 0);
                int color = active ? 0xFFFFFFFF : 0xFFA0A0A0;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 10);
                guiGraphics.drawString(MEStorageScreenMixin.this.font, label, textX, textY, color, false);
                guiGraphics.pose().popPose();
            }
        };
        ae2Wide$widthButton.setMessage(Component.translatable("gui.ae2_wide_wireless.width_toggle"));
        addToLeftToolbar(ae2Wide$widthButton);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void ae2Wide$applyRememberedWidth(CallbackInfo ci) {
        if (ae2Wide$hasFixedWidth()) {
            return;
        }

        boolean targetWide = TerminalWidthState.isWide();
        boolean currentlyWide = getStyle().getTerminalStyle().getSlotsPerRow() > 9;
        if (currentlyWide != targetWide) {
            Minecraft.getInstance().tell(() -> ae2Wide$openWithStyle(targetWide));
        }
    }

    @Unique
    private boolean ae2Wide$hasFixedWidth() {
        String className = getClass().getName();
        boolean universalMode = false;
        if (className.equals("de.mari_023.ae2wtlib.wct.WCTScreen")
                || className.equals("de.mari_023.ae2wtlib.wet.WETScreen")) {
            try {
                universalMode = Boolean.TRUE.equals(menu.getClass().getMethod("isWUT").invoke(menu));
            } catch (ReflectiveOperationException ignored) {
                // A regular wireless terminal remains switchable if an optional
                // AE2WTLib version does not expose the universal-mode flag.
            }
        }
        return TerminalScreenPolicy.hasFixedWidth(className, universalMode);
    }

    @Unique
    private void ae2Wide$toggleWidth() {
        ae2Wide$openWithStyle(TerminalWidthState.toggle());
    }

    @Unique
    private void ae2Wide$openWithStyle(boolean wide) {
        String stylePath = ae2Wide$getStylePath(wide);
        ScreenStyle nextStyle = StyleManager.loadStyleDoc(stylePath);
        var terminalStyle = nextStyle.getTerminalStyle();

        ((AEBaseScreenStyleAccessor) (Object) this).ae2Wide$setScreenStyle(nextStyle);
        ((MEStorageScreenStyleAccessor) (Object) this).ae2Wide$setTerminalStyle(terminalStyle);

        imageWidth = terminalStyle.getScreenWidth();
        resize(Minecraft.getInstance(), width, height);
        ((AEBaseScreenStyleAccessor) (Object) this).ae2Wide$repositionSlots();
        ((WidgetContainerStyleBridge) (Object) widgets).ae2Wide$setStyle(
                nextStyle,
                new Rect2i(leftPos, topPos, imageWidth, imageHeight),
                new Rect2i(0, 0, imageWidth, imageHeight));
    }

    @Unique
    private String ae2Wide$getStylePath(boolean wide) {
        String className = getClass().getName();
        if (className.equals("de.mari_023.ae2wtlib.wct.WCTScreen")) {
            return wide
                    ? "/screens/wtlib/wireless_crafting_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/wireless_crafting_terminal.json";
        }
        if (className.equals("de.mari_023.ae2wtlib.wet.WETScreen")) {
            return wide
                    ? "/screens/wtlib/wireless_pattern_encoding_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/wireless_pattern_encoding_terminal.json";
        }
        if (className.equals("com.moakiee.ae2lt.client.TianshuWirelessPatternEncodingTermScreen")) {
            return wide
                    ? "/screens/ae2_wide_wireless/wireless_tianshu_pattern_encoding_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/wireless_tianshu_pattern_encoding_terminal.json";
        }
        if (className.equals("com.moakiee.ae2lt.client.TianshuPatternEncodingTermScreen")) {
            return wide
                    ? "/screens/ae2_wide_wireless/tianshu_pattern_encoding_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/tianshu_pattern_encoding_terminal.json";
        }
        if ((Object) this instanceof PatternEncodingTermScreen<?>) {
            return wide
                    ? "/screens/terminals/pattern_encoding_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/pattern_encoding_terminal.json";
        }
        if ((Object) this instanceof CraftingTermScreen<?>) {
            return wide
                    ? "/screens/terminals/crafting_terminal.json"
                    : "/screens/ae2_wide_wireless/narrow/crafting_terminal.json";
        }
        return wide
                ? "/screens/terminals/terminal.json"
                : "/screens/ae2_wide_wireless/narrow/terminal.json";
    }

    @Unique
    private TypeFilterSwitchButton ae2Wide$createFilterButton(String titleKey,
            String hintKey, AEKeyType keyType) {
        var tooltip = List.<Component>of(Component.translatable(titleKey), Component.translatable(hintKey));
        return new TypeFilterSwitchButton(
                button -> ae2Wide$selectExclusiveType(
                        keyType, !ae2Wide$isOnlyEnabled(
                                menu.getClientKeyTypeSelection().enabledSet(), keyType)),
                tooltip);
    }

    @Unique
    private void ae2Wide$selectExclusiveType(AEKeyType target, boolean enabled) {
        var selection = menu.getClientKeyTypeSelection();
        if (!selection.keyTypes().containsKey(target)) {
            return;
        }

        Set<AEKeyType> desired = enabled
                ? Set.of(target)
                : new HashSet<>(selection.keyTypes().keySet());

        for (var keyType : desired) {
            menu.selectKeyType(keyType, true);
        }
        for (var keyType : new ArrayList<>(selection.enabledSet())) {
            if (!desired.contains(keyType)) {
                menu.selectKeyType(keyType, false);
            }
        }

        ae2Wide$syncFilterButtons();
        repo.updateView();
    }

    @Inject(method = "updateBeforeRender", at = @At("RETURN"))
    private void ae2Wide$updateFilterButtons(CallbackInfo ci) {
        ae2Wide$syncFilterButtons();
    }

    @Unique
    private void ae2Wide$syncFilterButtons() {
        if (ae2Wide$itemFilterButton == null || ae2Wide$fluidFilterButton == null) {
            return;
        }
        var selection = menu.getClientKeyTypeSelection();
        ae2Wide$itemFilterButton.setVisibility(selection.keyTypes().containsKey(AEKeyType.items()));
        ae2Wide$fluidFilterButton.setVisibility(selection.keyTypes().containsKey(AEKeyType.fluids()));
        ae2Wide$itemFilterButton.setChecked(ae2Wide$isOnlyEnabled(selection.enabledSet(), AEKeyType.items()));
        ae2Wide$fluidFilterButton.setChecked(ae2Wide$isOnlyEnabled(selection.enabledSet(), AEKeyType.fluids()));
    }

    @Unique
    private static boolean ae2Wide$isOnlyEnabled(List<AEKeyType> enabled, AEKeyType target) {
        return enabled.size() == 1 && enabled.contains(target);
    }
}
