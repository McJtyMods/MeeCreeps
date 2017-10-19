package mcjty.meecreeps;

import mcjty.meecreeps.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = MeeCreeps.MODID, name = "MeeCreeps",
        dependencies = "after:forge@[" + MeeCreeps.MIN_FORGE_VER + ",)",
        version = MeeCreeps.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)")
public class MeeCreeps {
    public static final String MODID = "meecreeps";
    public static final String VERSION = "0.0.1";
    public static final String MIN_FORGE_VER = "14.22.0.2464";

    @SidedProxy(clientSide = "mcjty.meecreeps.proxy.ClientProxy", serverSide = "mcjty.meecreeps.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static MeeCreeps instance;

    public static Logger logger;
    public static CreativeTabs creativeTab;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
//        creativeTab = new CompatCreativeTabs("meecreeps") {
//            @Override
//            protected Item getItem() {
//                return ModItems.manual;
//            }
//        };
        this.proxy.preInit(e);
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        this.proxy.init(e);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        this.proxy.postInit(e);
    }
}
