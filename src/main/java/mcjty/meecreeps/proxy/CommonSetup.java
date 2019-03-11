package mcjty.meecreeps.proxy;

import mcjty.lib.datafix.fixes.TileEntityNamespace;
import mcjty.lib.setup.DefaultCommonSetup;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class CommonSetup extends DefaultCommonSetup {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        CommandHandler.registerCommands();

        MeeCreeps.api.registerFactories();

        Config.readConfig();

        MeeCreepsMessages.registerMessages("meecreeps");

        // Initialization of blocks and items typically goes here:
        ModEntities.init();
    }

    @Override
    public void createTabs() {
        createTab("meecreeps", new ItemStack(ModItems.portalGunItem));
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(MeeCreeps.instance, new GuiProxy());
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        if (Config.mainConfig.hasChanged()) {
            Config.mainConfig.save();
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModFixs modFixs = FMLCommonHandler.instance().getDataFixer().init(MeeCreeps.MODID, 1);
        Map<String, String> oldToNewIdMap = new HashMap<>();

        event.getRegistry().register(new HeldCubeBlock());
        event.getRegistry().register(new PortalBlock());
        GameRegistry.registerTileEntity(PortalTileEntity.class, MeeCreeps.MODID + ":portalblock");

        // We used to accidentally register TEs with names like "minecraft:meecreeps_portalblock" instead of "meecreeps:portalblock".
        // Set up a DataFixer to map these incorrect names to the correct ones, so that we don't break old saved games.
        // @todo Remove all this if we ever break saved-game compatibility.
        oldToNewIdMap.put(MeeCreeps.MODID + "_portalblock", MeeCreeps.MODID + ":portalblock");
        oldToNewIdMap.put("minecraft:" + MeeCreeps.MODID + "_portalblock", MeeCreeps.MODID + ":portalblock");
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityNamespace(oldToNewIdMap, 1));
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
