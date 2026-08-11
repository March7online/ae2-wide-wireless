package dev.codex.ae2widewireless;

import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;

public final class WideSlotSemantics {
    public static final SlotSemantic MANUAL_CRAFTING_GRID = SlotSemantics.register(
            "AE2_WIDE_MANUAL_CRAFTING_GRID", true);
    public static final SlotSemantic MANUAL_CRAFTING_RESULT = SlotSemantics.register(
            "AE2_WIDE_MANUAL_CRAFTING_RESULT", false);

    private WideSlotSemantics() {
    }

    public static void init() {
        // Forces registration before a style document resolves the semantic names.
    }
}
