package mcjty.meecreeps;

import mcjty.lib.base.ModBase;
import mcjty.lib.proxy.IProxy;
import mcjty.meecreeps.api.IMeeCreepsApi;
import mcjty.meecreeps.commands.CommandClearActions;
import mcjty.meecreeps.commands.CommandListActions;
import mcjty.meecreeps.commands.CommandTestApi;
import mcjty.meecreeps.proxy.CommonSetup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

import java.util.Optional;
import java.util.function.Function;

@Mod(modid = MeeCreeps.MODID, name = "MeeCreeps",
        dependencies =
                "required-after:mcjtylib_ng@[" + MeeCreeps.MIN_MCJTYLIB_VER + ",);" +
                "after:forge@[" + MeeCreeps.MIN_FORGE_VER + ",)",
        version = MeeCreeps.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)")
public class MeeCreeps implements ModBase {
    public static final String MODID = "meecreeps";
    public static final String VERSION = "1.2.3";
    public static final String MIN_MCJTYLIB_VER = "3.1.0";
    public static final String MIN_FORGE_VER = "14.22.0.2464";

    @SidedProxy(clientSide = "mcjty.meecreeps.proxy.ClientProxy", serverSide = "mcjty.meecreeps.proxy.ServerProxy")
    public static IProxy proxy;
    public static CommonSetup setup = new CommonSetup();

    @Mod.Instance(MODID)
    public static MeeCreeps instance;

    public static MeeCreepsApi api = new MeeCreepsApi();

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        setup.preInit(e);
        proxy.preInit(e);
    }

    @Mod.EventHandler
    public void imcCallback(FMLInterModComms.IMCEvent event) {
        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
            if (message.key.equalsIgnoreCase("getMeeCreepsApi")) {
                Optional<Function<IMeeCreepsApi, Void>> value = message.getFunctionValue(IMeeCreepsApi.class, Void.class);
                if (value.isPresent()) {
                    value.get().apply(api);
                } else {
                    setup.getLogger().warn("Some mod didn't return a valid result with getMeeCreepsApi!");
                }
            }
        }
    }

    /**
     * Do your mod setup. Build whatever data structures you care about. Register recipes.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        setup.init(e);
        proxy.init(e);
    }

    /**
     * Handle interaction with other mods, complete your setup based on this.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        setup.postInit(e);
        proxy.postInit(e);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTestApi());
        event.registerServerCommand(new CommandClearActions());
        event.registerServerCommand(new CommandListActions());
    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void openManual(EntityPlayer entityPlayer, int i, String s) {
        // @todo
    }
}
