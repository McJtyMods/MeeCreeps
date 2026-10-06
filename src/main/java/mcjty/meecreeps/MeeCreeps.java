package mcjty.meecreeps;

import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.ModSetup;
import mcjty.meecreeps.setup.Registration;
import net.fabricmc.api.ModInitializer;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;

public class MeeCreeps implements ModInitializer {
    public static final String MODID = "meecreeps";
    public static final ModSetup setup = new ModSetup();
    public static final MeeCreepsApi api = new MeeCreepsApi();

    @Override public void onInitialize() {
        api.registerFactories();
        ConfigSetup.init();
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, ConfigSetup.SERVER_CONFIG);
        ConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);
        Registration.register();
        MeeCreepsMessages.registerMessages();
        FabricEventHandlers.register();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> mcjty.meecreeps.commands.ModCommands.register(dispatcher));
    }
}
