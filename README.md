# MeeCreeps

NeoForge 1.21.1 port of MeeCreeps. Requires Java 21, NeoForge 21.1.249 or later, and McJtyLib 1.21-9.0.21.20 or later. The One Probe and Interaction Wheel are optional. Hwyla and RedstoneFlux are not used.

Set `JAVA_HOME` to a Java 21 JDK, then build with `./gradlew build`. The installable mod is `build/libs/meecreeps-1.21.1-3.0.0.jar`. Install it alongside the NeoForge build of McJtyLib in your Minecraft 1.21.1 instance. The project uses ModDevGradle and Gradle 8.14.2.

Run development clients with `./gradlew runClient`, or `./gradlew runClient -PwithOptionalMods` to include TOP and Interaction Wheel. Use `./gradlew runServer` for a dedicated development server.

The build uses the shared `1.21_neo` workspace helpers, preferring `../gradletools.gradle` when available. Workspace projects supply McJtyLib, TOP, and Interaction Wheel when present; `-PinteractionWheelJar=/path/to/interaction-wheel.jar` overrides the compile-time Interaction Wheel dependency. Use `./gradlew publishMod` to publish the beta release with `CURSEFORGE_TOKEN` and/or `MODRINTH_TOKEN` set. Publishing declares McJtyLib as required and TOP and Interaction Wheel as optional.

Run the 21 NeoForge integration tests with `python3 scripts/run-gametests.py` or `./gradlew runGameTestServer`. Add `-PwithOptionalMods` to test with both optional integrations. They check energy simulation, persistence and component synchronization, cartridge recipes and remainders, action networking and entity saves, portal expiry and aimed height, cross-dimension teleportation, protected harvesting, crop replanting, moving a chest with its inventory, chest animation, lighting, and building around obstructed positions. Test sources and structures are excluded from the release jar.

The tested optional releases are The One Probe `1.21_neo-12.0.8-10` and Interaction Wheel `1.20-4.0.0`. Despite its version label, this Interaction Wheel release supports Minecraft 1.21.1 with NeoForge.

Portal guns and cartridges accept Forge Energy: 1,000 FE equals one portal charge. Cartridges can also be charged with ender pearls as before. Cartridge insertion/removal preserves destinations and partial FE charges. Sneak-use the portal gun on a block to manage its eight destinations; Delete removes the selected destination. Interaction Wheel also offers the destination screen while holding the gun. The message-repeat key defaults to B.

Commands: `/meecreeps list` (operator), `/meecreeps clear` (your tasks), and `/meecreeps clear all` (operator). Legacy `/creep_list`, `/creep_clear`, and `/creep_test` command names are available.

This port uses Minecraft 1.21.1 data components for item charge, destinations, and cube history, and registry-aware item serialization for carried inventories and saved tasks. Migration of existing 1.12.2 or 1.20.1 MeeCreeps saves is not implemented; use a new 1.21.1 world and the generated TOML configuration files.
