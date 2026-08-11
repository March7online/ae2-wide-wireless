# Terminal layout and auxiliary controls design

## Scope

This revision fixes the wide layouts of the normal crafting and pattern-encoding terminals, then completes the rewritten wide universal terminal without changing the behavior of other terminals.

## Wide normal terminals

- The lower 195-pixel AE2 crafting/pattern module is centered in the 357-pixel wide terminal at x=81.
- Only the storage grid is widened; the lower module keeps its original AE2 pixel geometry.
- Existing slot and EAEP offsets remain unchanged because they already target the centered x=81 origin.

## Universal terminal controls

The gap between the manual crafting area and pattern-encoding area contains one vertical control column:

1. Magnet-card settings button.
2. JEI transfer-target arrow, pointing at the active destination.
3. Trash inventory button.

All three use the AE2/AE2WTLib toolbar button frame. Magnet and trash open the native AE2 Wireless Terminals submenus and therefore use the native host inventories and client actions. The magnet button is hidden when no magnet card is installed.

## Player equipment strip

- Player preview outer bounds: y=0..76 relative to the strip.
- Four 18-pixel equipment slot backgrounds occupy the same top and bottom bounds.
- Their internal gaps are 1, 2, and 1 pixels, yielding top positions 0, 19, 39, and 58.
- The offhand slot remains aligned with the hotbar/bottom equipment row.

## Pixel corrections

- The manual crafting panel is shifted one pixel right so its left edge no longer overdraws the universal terminal border.
- The generated right-hand storage columns continue to copy a true middle storage cell, avoiding the former ninth/tenth-column seam.

## Compatibility

- AE2 19.2.17 on Minecraft 1.21.1 NeoForge.
- AE2WTLib 19.5.1 supplies magnet-card and trash menu behavior.
- JEI transfer routing remains client-local; recipe contents are still transferred through the existing JEI integration.
