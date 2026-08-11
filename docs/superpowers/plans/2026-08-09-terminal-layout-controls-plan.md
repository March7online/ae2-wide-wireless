# Terminal layout and controls implementation plan

**Goal:** Correct wide terminal geometry and integrate native AE2WTLib magnet/trash controls into the rewritten universal terminal.

**Architecture:** Keep AE2 screen styles and JEI handlers intact. Correct the texture-generation source of the normal-terminal offset. Make the universal host inherit AE2WTLib's crafting-terminal host so native trash and magnet submenus receive the host type and inventories they expect. Add screen widgets and position all controls through the screen-style JSON.

## Tasks

1. Add a layout regression test that inspects rendered texture alpha bounds and resolved screen-style coordinates.
2. Reproduce the current failures: lower module at x=50, 30x16 transfer control, uneven equipment spacing, and missing native submenu support.
3. Fix the texture generator to center the real 195px lower module at x=81 and regenerate the two textures.
4. Change `WideUniversalMenuHost` to derive from `WCTMenuHost` and use its crafting/trash inventories.
5. Mirror AE2WTLib's magnet/trash client actions in `WideUniversalMenu`.
6. Add magnet, transfer-arrow, and trash widgets to `WideUniversalScreen`; hide magnet when no card is present.
7. Update universal JSON and rendering coordinates for the center controls, equipment spacing, and one-pixel crafting-panel correction.
8. Bump the patch version, compile, process resources, run regression checks, build the JAR, and inspect its manifest/resources.
