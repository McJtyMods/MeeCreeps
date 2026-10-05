# MeeCreeps

Forge 1.20.1 port of MeeCreeps. Requires Java 17, Forge 47.1.3 or later, and McJtyLib 1.20-8.0.7 or later. The One Probe and Interaction Wheel are optional. Hwyla and RedstoneFlux are not used.

Build with `./gradlew build`. The installable mod is `build/libs/meecreeps-1.20.1-2.0.0.jar`.

Run development clients with `./gradlew runClient`, or `./gradlew runClient -PwithOptionalMods` to include TOP and Interaction Wheel. Use `./gradlew runServer` for a dedicated development server.

Run the ten Forge integration tests with `python3 scripts/run-gametests.py`. They check Forge Energy simulation and persistence, cartridge recipes, action networking and entity saves, portal expiry, protected harvesting, moving a chest with its inventory, and building around obstructed positions (including low ceilings, partial body overlap, jumping on a narrow pillar, and preserving materials on failed placement). Test sources and structures are excluded from the release jar. The script runs the GameTest JVM separately because the ForgeGradle 5 GameTest launcher can report a daemon shutdown after the tests have passed.

Portal guns and cartridges accept Forge Energy: 1,000 FE equals one portal charge. Cartridges can also be charged with ender pearls as before. Cartridge insertion/removal preserves destinations and partial FE charges. Sneak-use the portal gun on a block to manage its eight destinations; Delete removes the selected destination. Interaction Wheel also offers the destination screen while holding the gun. The message-repeat key defaults to B.

Commands: `/meecreeps list` (operator), `/meecreeps clear` (your tasks), and `/meecreeps clear all` (operator). Legacy `/creep_list`, `/creep_clear`, and `/creep_test` command names are available.

This port changes saved action, entity, and portal data formats, including dimensions and carried block states. It does not migrate 1.12.2 worlds or the old `meecreeps.cfg`; use a new 1.20.1 world and the generated TOML configuration files.
