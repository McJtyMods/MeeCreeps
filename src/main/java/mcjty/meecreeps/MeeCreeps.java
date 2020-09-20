package mcjty.meecreeps;

import mcjty.lib.base.ModBase;
import mcjty.lib.proxy.IProxy;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTESR;
import mcjty.meecreeps.commands.CommandClearActions;
import mcjty.meecreeps.commands.CommandListActions;
import mcjty.meecreeps.commands.CommandTestApi;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.setup.ModSetup;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MeeCreeps.MODID)
public class MeeCreeps implements ModBase {
    public static final String MODID = "meecreeps";
    public static final String VERSION = "1.3.1";
    public static final String MIN_MCJTYLIB_VER = "3.5.0";
    public static final String MIN_FORGE_VER = "14.22.0.2464";

    public static final ItemGroup TAB = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.CREEP_CUBE.get());
        }
    };

//    public static ModSetup setup = new ModSetup();
    public static MeeCreepsApi api = new MeeCreepsApi();
    private static MeeCreeps instance;

    public MeeCreeps() {
        instance = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(ModEntities.class);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(null, PortalTESR::new);
    }

    private void serverSetup(final FMLServerStartingEvent event) {
        event.getCommandDispatcher().register(
                Commands.literal(MeeCreeps.instance.getModId())
                    .then(CommandClearActions.register())
                    .then(CommandListActions.register())
                    .then(CommandTestApi.register())
        );
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
    }

    private void processIMC(final InterModProcessEvent event)
    {

        // some example code to receive and process InterModComms from other mods
    }

    @SidedProxy(clientSide = "mcjty.meecreeps.setup.ClientProxy", serverSide = "mcjty.meecreeps.setup.ServerProxy")
    public static IProxy proxy;

    /**
     * Run before anything else. Read your config, create blocks, items, etc, and
     * register them with the GameRegistry.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        setup.preInit(e);
        proxy.preInit(e);
    }

// todo: add back

//    @Mod.EventHandler
//    public void imcCallback(FMLInterModComms.IMCEvent event) {
//        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
//            if (message.key.equalsIgnoreCase("getMeeCreepsApi")) {
//                Optional<Function<IMeeCreepsApi, Void>> value = message.getFunctionValue(IMeeCreepsApi.class, Void.class);
//                if (value.isPresent()) {
//                    value.get().apply(api);
//                } else {
//                    setup.getLogger().warn("Some mod didn't return a valid result with getMeeCreepsApi!");
//                }
//            }
//        }
//    }

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
