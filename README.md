# AE2 Wide Wireless Terminal — NeoForge 1.21.1

- Replaces the AE2WTLib Wireless Universal Terminal menu with one 18-column screen.
- Keeps a real manual crafting grid and native AE2 pattern encoding workspace visible at the same time.
- Restores the direct item/fluid filters in the top bar.
- Adds a directional JEI target selector between the two work areas: recipes can either pull real ingredients from the ME network into the manual crafting grid or populate the pattern encoding area.
- Places a compact magnet-card filter button immediately left of the search field.
- Refreshes AE2WTLib's cached crafting-terminal selection when the combined universal terminal is opened, so an installed and enabled magnet card is not shadowed by another carried terminal.
- Places AE2WTLib's native wireless-terminal settings button above the JEI target selector, with the trash submenu below it.
- Makes JEI's green drag highlight follow that selected target instead of always highlighting the pattern grid.
- Integrates the vanilla player preview, armor slots and offhand slot into one continuous inventory band.
- Does not add a wide/narrow switch, terminal-mode switch, cache, or non-native management panels to the rewritten universal terminal.
- Keeps the existing wide/narrow switch, direct item/fluid filters, and compatibility layout fixes on the other supported terminals.

This rewrite contains a common menu and must be installed on both the client and the server.

Requires NeoForge 21.1.215+, Applied Energistics 2 19.2.17, and AE2WTLib 19.5.1. The two library versions are pinned because this release mixes into their concrete 1.21.1 menu APIs.
