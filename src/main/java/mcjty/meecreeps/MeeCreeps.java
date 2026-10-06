package mcjty.meecreeps;

import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.ModSetup;
import mcjty.meecreeps.setup.Registration;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import mcjty.meecreeps.api.IMeeCreepsApi;

import java.util.function.Function;

@Mod(MeeCreeps.MODID)
public class MeeCreeps {
    public static final String MODID = "meecreeps";
    public static final ModSetup setup = new ModSetup();
    public static final MeeCreepsApi api = new MeeCreepsApi();

    public MeeCreeps(IEventBus bus, ModContainer container) {
        api.registerFactories();
        ConfigSetup.init();
        Registration.register(bus);
        bus.addListener(MeeCreepsMessages::registerMessages);
        bus.addListener(this::imc);
        bus.addListener(this::enqueueImc);
        NeoForge.EVENT_BUS.register(new ForgeEventHandlers());
        NeoForge.EVENT_BUS.addListener(mcjty.meecreeps.commands.ModCommands::register);
        container.registerConfig(ModConfig.Type.COMMON, ConfigSetup.SERVER_CONFIG);
        container.registerConfig(ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);
    }

    private void enqueueImc(net.neoforged.fml.event.lifecycle.InterModEnqueueEvent event) {
        if (net.neoforged.fml.ModList.get().isLoaded("theoneprobe"))
            net.neoforged.fml.InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new mcjty.meecreeps.compat.TopCompatibility());
    }

    @SuppressWarnings("unchecked")
    private void imc(InterModProcessEvent event) {
        event.getIMCStream().filter(m -> m.method().equals("getMeeCreepsApi")).forEach(m -> {
            Object callback = m.messageSupplier().get();
            if (callback instanceof Function<?, ?>)
                ((Function<IMeeCreepsApi, ?>) callback).apply(api);
        });
    }
}
