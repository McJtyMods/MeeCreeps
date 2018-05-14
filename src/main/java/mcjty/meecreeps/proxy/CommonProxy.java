package mcjty.meecreeps.proxy;

import mcjty.lib.proxy.AbstractCommonProxy;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.ForgeEventHandlers;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.blocks.HeldCubeBlock;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalBlock;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.items.*;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;

@Mod.EventBusSubscriber
public class CommonProxy extends AbstractCommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        CommandHandler.registerCommands();

        MeeCreeps.api.registerFactories();

        File directory = e.getModConfigurationDirectory();
        mainConfig = new Configuration(new File(directory.getPath(), "meecreeps.cfg"));
        Config.readConfig(mainConfig);

        SimpleNetworkWrapper network = mcjty.lib.network.PacketHandler.registerMessages(MeeCreeps.MODID, "meecreeps");
        MeeCreepsMessages.registerMessages(network);

        // Initialization of blocks and items typically goes here:
        ModEntities.init();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(MeeCreeps.instance, new GuiProxy());
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new HeldCubeBlock());
        event.getRegistry().register(new PortalBlock());
        GameRegistry.registerTileEntity(PortalTileEntity.class, MeeCreeps.MODID + "_portalblock");
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new CreepCubeItem());
        event.getRegistry().register(new PortalGunItem());
        event.getRegistry().register(new EmptyPortalGunItem());
        event.getRegistry().register(new ProjectileItem());
        event.getRegistry().register(new CartridgeItem());
        event.getRegistry().register(new ItemBlock(ModBlocks.portalBlock).setRegistryName(ModBlocks.portalBlock.getRegistryName()));
    }
}
