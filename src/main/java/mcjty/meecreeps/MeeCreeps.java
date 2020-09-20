package mcjty.meecreeps;

import mcjty.lib.base.ModBase;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTESR;
import mcjty.meecreeps.commands.CommandClearActions;
import mcjty.meecreeps.commands.CommandListActions;
import mcjty.meecreeps.commands.CommandTestApi;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.entities.RenderMeeCreeps;
import mcjty.meecreeps.entities.RenderProjectile;
import mcjty.meecreeps.input.KeyBindings;
import mcjty.meecreeps.input.KeyInputHandler;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.render.BalloonRenderer;
import mcjty.meecreeps.setup.ClientSetup;
import mcjty.meecreeps.setup.Registration;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MeeCreeps.MODID)
public class MeeCreeps implements ModBase {
    public static final String MODID = "meecreeps";

    public static final ItemGroup TAB = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.CREEP_CUBE.get());
        }
    };

//    public static ModSetup setup = new ModSetup();
    public static MeeCreepsApi api = new MeeCreepsApi();
    public static MeeCreeps instance;

    public static final Logger LOGGER = LogManager.getLogger();

    public MeeCreeps() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigSetup.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::serverSetup);
        modEventBus.addListener(this::enqueueIMC);
        modEventBus.addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.EVENT_BUS.register(ModEntities.class);
    }

    private void setup(final FMLCommonSetupEvent event) {
//        NetworkRegistry.INSTANCE.registerGuiHandler(MeeCreeps.instance, new GuiProxy());
        CommandHandler.registerCommands();
        PacketHandler.registerMessages("meecreeps");
        MeeCreeps.api.registerFactories();
        // todo: load config
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(null, PortalTESR::new);

//        OBJLoader.INSTANCE.addDomain(MeeCreeps.MODID);
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.PROJECTILE_ENTITY.get(), RenderProjectile::new);
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.MEECREEPS_ENTITY.get(), new RenderMeeCreeps.Factory());

        MinecraftForge.EVENT_BUS.register(ClientSetup.class);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();
        BalloonRenderer.renderBalloon();
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

    @Override
    public String getModId() {
        return MODID;
    }
}
