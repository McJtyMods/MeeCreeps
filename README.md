# MeeCreeps

MeeCreeps for Minecraft 26.3 and NeoForge 26.3.0.83 or later. Requires Java 25. The One Probe is optional. Interaction Wheel integration is disabled. McJtyLib 26.3-11.0.0 or newer is required and supplies command routing, packet helpers, and saved-task support.

Set `JAVA_HOME` to a Java 25 **JDK**, then build with `./gradlew build`. Install `build/libs/meecreeps-26.3-5.0.1.jar` in your Minecraft 26.3 NeoForge instance. The project uses ModDevGradle and Gradle 9.2.1.

Run a development client with `./gradlew runClient`. The shared `mcjtylib()`, `top()`, and `jei()` helpers include McJtyLib, The One Probe, and JEI for development. Use `./gradlew runServer` for a dedicated development server.

The build uses the published `MultiWorkspace/26.3_neo/gradletools.gradle`, or the root shared script in an integrated workspace. No local copy is needed. It pins the McJtyLib Maven artifact to `26.3-11.0.0.3`, while the mod dependency requirement stays at `26.3-11.0.0`. Workspace projects supply McJtyLib and The One Probe when present. Generate client or server data with `./gradlew runClientData` or `./gradlew runServerData`; both use `run-data` and write resources to `src/generated/resources`. In a workspace, add `-Penable_individual_runs` to enable this project's client, server, and data runs. Use `./gradlew publishMod` to publish the beta release with `CURSEFORGE_TOKEN` and/or `MODRINTH_TOKEN` set. Publishing declares McJtyLib as required and The One Probe as optional.

Run the 23 integration tests with `python3 scripts/run-gametests.py` or `./gradlew runGameTestServer`. The One Probe and JEI are included in development test runs. Tests cover transactional energy charging and rollback, persistence and component synchronization, cartridge recipes and remainders, action networking and ownership checks, entity saves, portal expiry and aimed height, cross-dimension teleportation, protected harvesting, crop replanting, moving a chest with its inventory, chest animation, lighting, building around obstructions, and material shortage feedback. Test sources and structures are excluded from the release jar.

Portal guns and cartridges accept NeoForge energy: 1,000 FE equals one portal charge. Cartridges can also be charged with ender pearls. Cartridge insertion/removal preserves destinations and partial FE charges. Sneak-use the portal gun on a block to manage its eight destinations; Delete removes the selected destination. This built-in destination screen works without Interaction Wheel. The message-repeat key defaults to B.

Commands: `/meecreeps list` (operator), `/meecreeps clear` (your tasks), and `/meecreeps clear all` (operator). Legacy `/creep_list`, `/creep_clear`, and `/creep_test` command names are available.

This port uses data components for item charge, destinations, and cube history, registry-aware item serialization for carried inventories and saved tasks, and the 26.3 render-state APIs for entities, portals, and GUIs. The `meecreeps:actions` task identifier and save layout remain unchanged when moving to McJtyLib. Worlds from older Minecraft versions have not been migration-tested; use a new 26.3 world and the generated TOML configuration files.
