package mcjty.meecreeps;

import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.api.IMeeCreepsApi;
import mcjty.meecreeps.commands.CommandTestApi;
import mcjty.meecreeps.items.ModItems;
import mcjty.meecreeps.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Function;

@Mod(modid = MeeCreeps.MODID, name = "MeeCreeps",
        dependencies = "after:forge@[" + MeeCreeps.MIN_FORGE_VER + ",)",
        version = MeeCreeps.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)")
public class MeeCreeps {
    public static final String MODID = "meecreeps";
    public static final String VERSION = "0.1.1beta";
    public static final String MIN_FORGE_VER = "14.22.0.2464";

    @SidedProxy(clientSide = "mcjty.meecreeps.proxy.ClientProxy", serverSide = "mcjty.meecreeps.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static MeeCreeps instance;

    public static MeeCreepsApi api = new MeeCreepsApi();

    public static Logger logger;

    public static CreativeTabs creativeTab = new CreativeTabs("meecreeps") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModItems.portalGunItem);
        }
    };


    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger = e.getModLog();
        this.proxy.preInit(e);
    }

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if (message.key.equalsIgnoreCase("getMeeCreepsApi")) {
                Optional<Function<IMeeCreepsApi, Void>> value = message.getFunctionValue(IMeeCreepsApi.class, Void.class);
                if (value.isPresent()) {
                    value.get().apply(api);
                } else {
                    logger.warn("Some mod didn't return a valid result with getMeeCreepsApi!");
                }
            }
        }
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

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTestApi());
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {
        ServerActionManager.clearInstance();
    }

}
