package mcjty.meecreeps.setup;

import mcjty.lib.setup.DefaultModSetup;
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

public class ModSetup extends DefaultModSetup {

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        NetworkRegistry.INSTANCE.registerGuiHandler(MeeCreeps.instance, new GuiProxy());

        CommandHandler.registerCommands();

        MeeCreepsMessages.registerMessages("meecreeps");

        ModEntities.init();
    }

    @Override
    protected void setupModCompat() {

    }

    @Override
    protected void setupConfig() {
        MeeCreeps.api.registerFactories();
        ConfigSetup.init();
    }

    @Override
    public void createTabs() {
        createTab("meecreeps", () -> new ItemStack(ModItems.portalGunItem));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
        ConfigSetup.postInit();
    }
}
