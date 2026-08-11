package dev.codex.ae2widewireless;

public final class TerminalScreenPolicyTest {
    public static void main(String[] args) {
        assertFixed("dev.codex.ae2widewireless.client.WideUniversalScreen", false, true);
        assertFixed("de.mari_023.ae2wtlib.wct.WCTScreen", true, true);
        assertFixed("de.mari_023.ae2wtlib.wct.WCTScreen", false, false);
        assertFixed("de.mari_023.ae2wtlib.wet.WETScreen", true, true);
        assertFixed("de.mari_023.ae2wtlib.wet.WETScreen", false, false);
        assertFixed("com.moakiee.ae2lt.client.TianshuPatternEncodingTermScreen", true, false);

        if (!TerminalWidthState.isWide()) {
            throw new AssertionError("Terminal width state must default to wide");
        }
        if (TerminalWidthState.toggle()) {
            throw new AssertionError("First toggle must select narrow mode");
        }
        assertEquals(TerminalWidthState.getTianshuStylePath(false),
                "/screens/ae2_wide_wireless/narrow/tianshu_pattern_encoding_terminal.json");
        assertEquals(TerminalWidthState.getTianshuStylePath(true),
                "/screens/ae2_wide_wireless/narrow/wireless_tianshu_pattern_encoding_terminal.json");
        if (!TerminalWidthState.toggle()) {
            throw new AssertionError("Second toggle must restore wide mode");
        }
        assertEquals(TerminalWidthState.getTianshuStylePath(false),
                "/screens/ae2_wide_wireless/tianshu_pattern_encoding_terminal.json");
        assertEquals(TerminalWidthState.getTianshuStylePath(true),
                "/screens/ae2_wide_wireless/wireless_tianshu_pattern_encoding_terminal.json");
    }

    private static void assertFixed(String screenClassName, boolean universalMode, boolean expected) {
        boolean actual = TerminalScreenPolicy.hasFixedWidth(screenClassName, universalMode);
        if (actual != expected) {
            throw new AssertionError(screenClassName + " universal=" + universalMode
                    + " expected fixed=" + expected + " but got " + actual);
        }
    }

    private static void assertEquals(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected '" + expected + "' but got '" + actual + "'");
        }
    }
}
