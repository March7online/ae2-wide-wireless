# Universal terminal toolbar button swap implementation plan

> **For the implementing agent:** Execute these steps in order and keep the existing dual-workspace layout unchanged.

**Goal:** Move the magnet-card button to the left navigation toolbar and place the native AE2WTLib wireless-terminal settings button in its former central position.

**Architecture:** Keep `WideUniversalScreen` as the owner of both controls. Register the magnet button through AE2's left-toolbar API so the toolbar controls its layout; register the settings button in the style-backed widget container so it occupies the existing top central slot. Because AE2WTLib's settings screen hard-casts its parent/menu to `WCTScreen`/`WCTMenu`, use a narrow adapter screen that reuses its style, components and `TerminalSettingsPacket` with the rewritten menu.

**Technology stack:** Java 21, NeoForge 1.21.1, AE2 19.2.17, AE2WTLib 19.5.1, AE2 JSON screen styles, PowerShell regression checks.

---

### Task 1: Lock the requested positions with a failing regression test

**Files:**
- Modify: `tests/Test-LayoutRegression.ps1`

- [ ] Replace the expected central `magnetCardMenuButton` with `wirelessTerminalSettingsButton`.
- [ ] Assert that the magnet button no longer has a fixed central style entry.
- [ ] Run `powershell -ExecutionPolicy Bypass -File tests/Test-LayoutRegression.ps1` and confirm it fails because the settings widget is absent.

### Task 2: Implement the button swap

**Files:**
- Modify: `src/main/java/dev/codex/ae2widewireless/client/WideUniversalScreen.java`
- Modify: `src/main/resources/assets/ae2/screens/ae2_wide_wireless/wide_universal_terminal.json`

- [ ] Add the magnet button through `addToLeftToolbar(...)`, preserving its native magnet menu and visibility rules.
- [ ] Add a `TERMINAL_SETTINGS` button that opens `WirelessTerminalSettingsScreen`.
- [ ] Replace the central style key while keeping the existing coordinates `(left=147, bottom=164)`.
- [ ] Rerun the regression test and confirm it passes.

### Task 3: Package and verify the release

**Files:**
- Modify: `gradle.properties`
- Modify: `README.md`

- [ ] Bump the patch version to `2.1.4` and document the control placement.
- [ ] Compile the Java sources against the verified cached dependency set.
- [ ] Run resource processing, package the JAR, rerun layout checks against sources and the unpacked JAR, and calculate SHA-256.
