package mcjty.meecreeps.config;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.proxy.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.util.HashSet;
import java.util.Set;

public class Config {

    private static final String CATEGORY_GENERAL = "general";
    private static final String CATEGORY_PERMISSON = "permission";

    public static void readConfig() {
        Configuration cfg = CommonProxy.config;
        try {
            cfg.load();
            initGeneralConfig(cfg);
            initPermissionConfig(cfg);
        } catch (Exception e1) {
            MeeCreeps.logger.log(Level.ERROR, "Problem loading config file!", e1);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    public static int portalTimeout = 30*20;
    public static int portalTimeoutAfterEntry = 5*20;
    public static int maxCharge = 64;
    public static int chargesPerEnderpearl = 4;

    public static int meeCreepBoxMaxUsage = -1;
    public static int maxMeecreepsPerPlayer = 4;

    public static float meeCreepVolume = 1.0f;
    public static float teleportVolume = 1.0f;

    public static int messageTimeout = 120;
    public static int messageX = 0;
    public static int messageY = 10;

    public static int maxSpawnCount = 60;
    public static int maxTreeBlocks = 2000;

    public static float delayAtHardness = 10;
    public static float delayFactor = 0.75f;

    public static Set<String> allowedActions = new HashSet<>();

    // @todo
    // config for type of pickaxe

    private static void initGeneralConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        portalTimeout = cfg.getInt("portalTimeout", CATEGORY_GENERAL, portalTimeout, 1, 1000000, "Amount of ticks until the portalpair disappears");
        portalTimeoutAfterEntry = cfg.getInt("portalTimeoutAfterEntry", CATEGORY_GENERAL, portalTimeoutAfterEntry, 1, 1000000, "Amount of ticks until the portalpair disappears after an entity has gone through");
        maxCharge = cfg.getInt("maxCharge", CATEGORY_GENERAL, maxCharge, 1, 1000000, "Maximum charge in a portalgun/cartridge");
        chargesPerEnderpearl = cfg.getInt("chargesPerEnderpearl", CATEGORY_GENERAL, chargesPerEnderpearl, 1, 1000000, "Number of charges per enderpearl");
        meeCreepBoxMaxUsage = cfg.getInt("meeCreepBoxMaxUsage", CATEGORY_GENERAL, meeCreepBoxMaxUsage, -1, 1000000, "Maximum number of uses for a single MeeCreep box (-1 means unlimited)");
        maxMeecreepsPerPlayer = cfg.getInt("maxMeecreepsPerPlayer", CATEGORY_GENERAL, maxMeecreepsPerPlayer, -1, 1000000, "Maximum number of active MeeCreeps per player (-1 means unlimited)");

        meeCreepVolume = cfg.getFloat("meeCreepVolume", CATEGORY_GENERAL, meeCreepVolume, 0, 1, "Volume of the MeeCreep");
        teleportVolume = cfg.getFloat("teleportVolume", CATEGORY_GENERAL, teleportVolume, 0, 1, "Volume of the Portal Gun");

        messageX = cfg.getInt("messageX", CATEGORY_GENERAL, messageX, -100, 100, "Balloon horizontal postion: 0 means centered, positive means percentage offset from left side, negative means percentage offset from right side");
        messageY = cfg.getInt("messageY", CATEGORY_GENERAL, messageY, -100, 100, "Balloon vertical position: 0 means centered, positive means percentage offset from top side, negative means percentage offset from bottom side");
        messageTimeout = cfg.getInt("messageTimeout", CATEGORY_GENERAL, messageTimeout, 1, 10000, "Number of ticks (20 ticks per second) before the balloon message disappears");

        maxSpawnCount = cfg.getInt("maxSpawnCount", CATEGORY_GENERAL, maxSpawnCount, 1, 200, "Spawn cap for an angry MeeCreep (a MeeCreep with a box)");
        maxTreeBlocks = cfg.getInt("maxTreeBlocks", CATEGORY_GENERAL, maxTreeBlocks, 1, 100000, "Maximum number of tree blocks a single MeeCreep can chop down");

        delayAtHardness = cfg.getFloat("delayAtHardness", CATEGORY_GENERAL, delayAtHardness, 0, 10000000, "Delay harvest of blocks if hardness is bigger then this value");
        delayFactor = cfg.getFloat("delayFactor", CATEGORY_GENERAL, delayFactor, 0, 1000, "Speed modifier for harvesting (i.e. how much faster a MeeCreep is compared to a player)");
    }

    private static void initPermissionConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_PERMISSON, "Permission configuration");
        for (MeeCreepsApi.Factory factory : MeeCreeps.api.getFactories()) {
            boolean allowed = cfg.getBoolean("allowed_" + factory.getId(), CATEGORY_PERMISSON, true, "");
            if (allowed) {
                allowedActions.add(factory.getId());
            }
        }
    }

}
