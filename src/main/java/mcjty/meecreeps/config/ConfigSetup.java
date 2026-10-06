package mcjty.meecreeps.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;

import java.util.HashSet;
import java.util.Set;

public class ConfigSetup {

    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_PERMISSON = "permission";

    public static ModConfigSpec.IntValue portalTimeout;
    public static ModConfigSpec.IntValue portalTimeoutAfterEntry;
    public static ModConfigSpec.IntValue maxCharge;
    public static ModConfigSpec.IntValue chargesPerEnderpearl;

    public static ModConfigSpec.IntValue meeCreepBoxMaxUsage;
    public static ModConfigSpec.IntValue maxMeecreepsPerPlayer;

    public static ModConfigSpec.DoubleValue meeCreepVolume;
    public static ModConfigSpec.DoubleValue teleportVolume;

    public static ModConfigSpec.IntValue messageTimeout;
    public static ModConfigSpec.IntValue messageX;
    public static ModConfigSpec.IntValue messageY;

    public static ModConfigSpec.IntValue maxSpawnCount;
    public static ModConfigSpec.IntValue maxTreeBlocks;

    public static ModConfigSpec.DoubleValue delayAtHardness;
    public static ModConfigSpec.DoubleValue delayFactor;

    public static Set<String> allowedActions = new HashSet<>();

    private static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    static {
        SERVER_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);
        CLIENT_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        portalTimeout = SERVER_BUILDER
                .comment("Amount of ticks until the portalpair disappears")
                .defineInRange("portalTimeout", 30 * 20, 1, 1000000);
        portalTimeoutAfterEntry = SERVER_BUILDER
                .comment("Amount of ticks until the portalpair disappears after an entity has gone through")
                .defineInRange("portalTimeoutAfterEntry", 5 * 20, 1, 1000000);
        maxCharge = SERVER_BUILDER
                .comment("Maximum charge in a portalgun/cartridge")
                .defineInRange("maxCharge", 64, 1, 1000000);
        chargesPerEnderpearl = SERVER_BUILDER
                .comment("Number of charges per enderpearl")
                .defineInRange("chargesPerEnderpearl", 4, 1, 1000000);
        meeCreepBoxMaxUsage = SERVER_BUILDER
                .comment("Maximum number of uses for a single MeeCreep box (-1 means unlimited)")
                .defineInRange("meeCreepBoxMaxUsage", -1, -1, 1000000);
        maxMeecreepsPerPlayer = SERVER_BUILDER
                .comment("Maximum number of active MeeCreeps per player (-1 means unlimited)")
                .defineInRange("maxMeecreepsPerPlayer", 4, -1, 1000000);

        meeCreepVolume = SERVER_BUILDER
                .comment("Volume of the MeeCreep")
                .defineInRange("meeCreepVolume", 1.0, 0, 1);
        teleportVolume = SERVER_BUILDER
                .comment("Volume of the Portal Gun")
                .defineInRange("teleportVolume", 1.0, 0, 1);

        messageX = CLIENT_BUILDER
                .comment("Balloon horizontal postion: 0 means centered, positive means percentage offset from left side, negative means percentage offset from right side")
                .defineInRange("messageX", 0, -100, 100);
        messageY = CLIENT_BUILDER
                .comment("Balloon vertical position: 0 means centered, positive means percentage offset from top side, negative means percentage offset from bottom side")
                .defineInRange("messageY", 10, -100, 100);
        messageTimeout = CLIENT_BUILDER
                .comment("Number of ticks (20 ticks per second) before the balloon message disappears")
                .defineInRange("messageTimeout", 120, 1, 10000);

        maxSpawnCount = SERVER_BUILDER
                .comment("Spawn cap for an angry MeeCreep (a MeeCreep with a box)")
                .defineInRange("maxSpawnCount", 60, 1, 200);
        maxTreeBlocks = SERVER_BUILDER
                .comment("Maximum number of tree blocks a single MeeCreep can chop down")
                .defineInRange("maxTreeBlocks", 2000, 1, 100000);

        delayAtHardness = SERVER_BUILDER
                .comment("Delay harvest of blocks if hardness is bigger then this value")
                .defineInRange("delayAtHardness", 10.0, 0, 10000000);
        delayFactor = SERVER_BUILDER
                .comment("Speed modifier for harvesting (i.e. how much faster a MeeCreep is compared to a player)")
                .defineInRange("delayFactor", 0.75, 0, 1000);

        SERVER_BUILDER.pop();
        CLIENT_BUILDER.pop();
    }

    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec CLIENT_CONFIG;

    public static void init() {
        SERVER_BUILDER.push("permission");
        for (MeeCreepsApi.Factory factory : MeeCreeps.api.getFactories()) {
            actionPermissions.put(factory.getId(), SERVER_BUILDER.define("allowed_" + factory.getId(), true));
        }
        SERVER_BUILDER.pop();
        SERVER_CONFIG = SERVER_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static final java.util.Map<String, ModConfigSpec.BooleanValue> actionPermissions = new java.util.HashMap<>();

    public static boolean isAllowed(String id) {
        var permission = actionPermissions.get(id);
        return permission != null && permission.get();
    }
}
