package mcjty.meecreeps.setup;

import mcjty.lib.setup.DefaultCommonSetup;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.ForgeEventHandlers;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.items.ModItems;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonSetup extends DefaultCommonSetup {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(MeeCreeps.instance, new GuiProxy());

        CommandHandler.registerCommands();
        MeeCreeps.api.registerFactories();

        MeeCreepsMessages.registerMessages("meecreeps");

        ConfigSetup.init();
        ModEntities.init();
    }

    @Override
    protected void setupModCompat() {

    }

    @Override
    public void createTabs() {
        createTab("meecreeps", new ItemStack(ModItems.portalGunItem));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ConfigSetup.postInit();
    }
}
