# MeeCreeps

MeeCreeps for Minecraft 26.2 with Fabric Loader 0.19.3 or later. Requires Java 25, Fabric API 0.155.2+26.2 or later, and Forge Config API Port 26.2.1 or later (Fabric edition). Team Reborn Energy 5.0.0 is bundled in the mod jar.

The One Probe integration is temporarily disabled. Its source is preserved under `src/disabled/java` for a future Fabric release. Interaction Wheel integration remains disabled; the built-in portal destination screen is available. McJtyLib is not required.

Set `JAVA_HOME` to Java 25, then build with `./gradlew build`. Install `build/libs/meecreeps-fabric-26.2-4.0.0.jar`, Fabric API, and Forge Config API Port in your Minecraft 26.2 Fabric instance. The project uses Fabric Loom 1.17.17 and Gradle 9.5.1.

Run a development client with `./gradlew runClient` or a dedicated server with `./gradlew runServer`. Development worlds use `run-fabric` and `run-server-fabric`.

Run the 24 MeeCreeps integration tests with `python3 scripts/run-gametests.py` or `./gradlew runGameTest`. They also run as part of `./gradlew build`. Fabric includes an additional framework test. Tests cover item interaction before chest opening, transactional energy charging and rollback, persistence and component synchronization, cartridge recipes and remainders, action payloads and ownership checks, entity saves, portal expiry and aimed height, cross-dimension teleportation, protected harvesting, crop replanting, moving a chest with its inventory, chest animation, lighting, building around obstructions, and material shortage feedback. Test sources and structures are excluded from the release jar. The original NeoForge test registration and port tests are archived under `src/disabled/gametest`.

Portal guns and cartridges accept Fabric energy through Team Reborn Energy: 1,000 energy units equal one portal charge. Cartridges can also be charged with ender pearls. Cartridge insertion/removal preserves destinations and partial charges. Sneak-use the portal gun on a block to manage its eight destinations; Delete removes the selected destination. The message-repeat key defaults to B.

Commands: `/meecreeps list` (operator), `/meecreeps clear` (your tasks), and `/meecreeps clear all` (operator). Legacy `/creep_list`, `/creep_clear`, and `/creep_test` command names are available.

The mod uses data components for item charge, destinations, and cube history, registry-aware item serialization for carried inventories and saved tasks, and the 26.2 render-state APIs for entities, portals, and GUIs. Fabric callbacks handle lifecycle, interaction, networking, and protected block breaking. The public API is available as `MeeCreeps.api` for other mods to register action factories during initialization.

Existing worlds from older Minecraft versions or NeoForge have not been migration-tested. Use a new 26.2 world and the generated TOML configuration files.
