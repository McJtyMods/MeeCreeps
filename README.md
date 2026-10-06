# MeeCreeps

MeeCreeps for Minecraft 26.2 and NeoForge 26.2.0.75 or later. Requires Java 25. The One Probe is optional; the tested release is `26.2_neo-15.0.1-2`. Interaction Wheel integration is disabled because it is unavailable for 26.2. McJtyLib is no longer required: the mod handles its own networking, sounds, and saved tasks.

Set `JAVA_HOME` to a Java 25 **JDK**, then build with `./gradlew build`. Install `build/libs/meecreeps-26.2-4.0.0.jar` in your Minecraft 26.2 NeoForge instance. The project uses ModDevGradle and Gradle 9.2.1.

Run a development client with `./gradlew runClient`, or add `-PwithOptionalMods` to include The One Probe. Use `./gradlew runServer` for a dedicated development server.

Run the 23 integration tests with `python3 scripts/run-gametests.py` or `./gradlew runGameTestServer`. Add `-PwithOptionalMods` to test with The One Probe installed. Tests cover transactional energy charging and rollback, persistence and component synchronization, cartridge recipes and remainders, action networking and ownership checks, entity saves, portal expiry and aimed height, cross-dimension teleportation, protected harvesting, crop replanting, moving a chest with its inventory, chest animation, lighting, building around obstructions, and material shortage feedback. Test sources and structures are excluded from the release jar.

Portal guns and cartridges accept NeoForge energy: 1,000 FE equals one portal charge. Cartridges can also be charged with ender pearls. Cartridge insertion/removal preserves destinations and partial FE charges. Sneak-use the portal gun on a block to manage its eight destinations; Delete removes the selected destination. This built-in destination screen works without Interaction Wheel. The message-repeat key defaults to B.

Commands: `/meecreeps list` (operator), `/meecreeps clear` (your tasks), and `/meecreeps clear all` (operator). Legacy `/creep_list`, `/creep_clear`, and `/creep_test` command names are available.

This port uses data components for item charge, destinations, and cube history, registry-aware item serialization for carried inventories and saved tasks, and the 26.2 render-state APIs for entities, portals, and GUIs. Existing MeeCreeps worlds from older Minecraft versions have not been migration-tested; use a new 26.2 world and the generated TOML configuration files.
