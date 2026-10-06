package mcjty.meecreeps.varia;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class LevelTools {
    public static MinecraftServer server() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static ServerLevel overworld() {
        return server().overworld();
    }

    public static ServerLevel getWorld(ResourceKey<Level> key) {
        return server().getLevel(key);
    }
}
