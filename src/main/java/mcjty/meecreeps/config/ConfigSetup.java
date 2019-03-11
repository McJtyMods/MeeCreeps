package mcjty.meecreeps.config;

import mcjty.lib.thirteen.ConfigSpec;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ConfigSetup {

    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_PERMISSON = "permission";

    public static Configuration mainConfig;

    public static void init() {
        mainConfig = new Configuration(new File(MeeCreeps.setup.getModConfigDir().getPath(), "meecreeps.cfg"));
        try {
            mainConfig.load();
            initConfig(mainConfig);
        } catch (Exception e1) {
            MeeCreeps.setup.getLogger().log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (mainConfig.hasChanged()) {
                mainConfig.save();
            }
        }
    }

    public static ConfigSpec.IntValue portalTimeout;
    public static ConfigSpec.IntValue portalTimeoutAfterEntry;
    public static ConfigSpec.IntValue maxCharge;
    public static ConfigSpec.IntValue chargesPerEnderpearl;

    public static ConfigSpec.IntValue meeCreepBoxMaxUsage;
    public static ConfigSpec.IntValue maxMeecreepsPerPlayer;

    public static ConfigSpec.DoubleValue meeCreepVolume;
    public static ConfigSpec.DoubleValue teleportVolume;

    public static ConfigSpec.IntValue messageTimeout;
    public static ConfigSpec.IntValue messageX;
    public static ConfigSpec.IntValue messageY;

    public static ConfigSpec.IntValue maxSpawnCount;
    public static ConfigSpec.IntValue maxTreeBlocks;

    public static ConfigSpec.DoubleValue delayAtHardness;
    public static ConfigSpec.DoubleValue delayFactor;

    public static Set<String> allowedActions = new HashSet<>();

    private static final ConfigSpec.Builder SERVER_BUILDER = new ConfigSpec.Builder();
    private static final ConfigSpec.Builder CLIENT_BUILDER = new ConfigSpec.Builder();

    static {
        SERVER_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);
        CLIENT_BUILDER.comment("General configuration").push(CATEGORY_GENERAL);

        portalTimeout = SERVER_BUILDER
                .comment("Amount of ticks until the portalpair disappears")
                .defineInRange("portalTimeout", 30*20, 1, 1000000);
        portalTimeoutAfterEntry = SERVER_BUILDER
                .comment("Amount of ticks until the portalpair disappears after an entity has gone through")
                .defineInRange("portalTimeoutAfterEntry", 5*20, 1, 1000000);
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

    public static ConfigSpec SERVER_CONFIG;
    public static ConfigSpec CLIENT_CONFIG;


    private static void initConfig(Configuration cfg) {
        SERVER_CONFIG = SERVER_BUILDER.build(cfg);
        CLIENT_CONFIG = CLIENT_BUILDER.build(cfg);

        initPermissionConfig(cfg);
    }

    // @todo
    // config for type of pickaxe

    private static void initPermissionConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_PERMISSON, "Permission configuration");
        for (MeeCreepsApi.Factory factory : MeeCreeps.api.getFactories()) {
            boolean allowed = cfg.getBoolean("allowed_" + factory.getId(), CATEGORY_PERMISSON, true, "");
            if (allowed) {
                allowedActions.add(factory.getId());
            }
        }
    }

    public static void postInit() {
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }
}
