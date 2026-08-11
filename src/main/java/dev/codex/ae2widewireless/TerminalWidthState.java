package dev.codex.ae2widewireless;

/** Client-local width preference shared by switchable terminal screens. */
public final class TerminalWidthState {
    private static boolean wide = true;

    private TerminalWidthState() {
    }

    public static boolean isWide() {
        return wide;
    }

    /** Toggles the preference and returns the newly selected state. */
    public static boolean toggle() {
        wide = !wide;
        return wide;
    }

    public static String getTianshuStylePath(boolean wireless) {
        if (wide) {
            return wireless
                    ? "/screens/ae2_wide_wireless/wireless_tianshu_pattern_encoding_terminal.json"
                    : "/screens/ae2_wide_wireless/tianshu_pattern_encoding_terminal.json";
        }
        return wireless
                ? "/screens/ae2_wide_wireless/narrow/wireless_tianshu_pattern_encoding_terminal.json"
                : "/screens/ae2_wide_wireless/narrow/tianshu_pattern_encoding_terminal.json";
    }
}
