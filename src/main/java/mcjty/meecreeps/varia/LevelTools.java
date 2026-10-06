package mcjty.meecreeps.varia;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;


public final class LevelTools {
    private static MinecraftServer currentServer;
    public static void setServer(MinecraftServer server) { currentServer = server; }
    public static MinecraftServer server() {
        return currentServer;
    }

    public static ServerLevel overworld() {
        return server().overworld();
    }

    public static ServerLevel getWorld(ResourceKey<Level> key) {
        return server().getLevel(key);
    }
}
