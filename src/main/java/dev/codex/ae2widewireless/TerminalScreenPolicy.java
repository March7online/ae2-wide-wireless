package dev.codex.ae2widewireless;

/** Width-switch policy kept independent from optional terminal classes. */
public final class TerminalScreenPolicy {
    private static final String REWRITTEN_UNIVERSAL =
            "dev.codex.ae2widewireless.client.WideUniversalScreen";
    private static final String WIRELESS_CRAFTING = "de.mari_023.ae2wtlib.wct.WCTScreen";
    private static final String WIRELESS_ENCODING = "de.mari_023.ae2wtlib.wet.WETScreen";

    private TerminalScreenPolicy() {
    }

    public static boolean hasFixedWidth(String screenClassName, boolean universalMode) {
        if (REWRITTEN_UNIVERSAL.equals(screenClassName)) {
            return true;
        }
        return universalMode
                && (WIRELESS_CRAFTING.equals(screenClassName)
                        || WIRELESS_ENCODING.equals(screenClassName));
    }
}
