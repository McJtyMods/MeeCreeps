package mcjty.meecreeps;

import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.ModSetup;
import mcjty.meecreeps.setup.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import mcjty.meecreeps.api.IMeeCreepsApi;

import java.util.function.Function;

@Mod(MeeCreeps.MODID)
public class MeeCreeps {
    public static final String MODID = "meecreeps";
    public static final ModSetup setup = new ModSetup();
    public static final MeeCreepsApi api = new MeeCreepsApi();

    public MeeCreeps() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        api.registerFactories();
        ConfigSetup.init();
        Registration.register(bus);
        bus.addListener(this::commonSetup);
        bus.addListener(this::imc);
        bus.addListener(this::enqueueImc);
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
        MinecraftForge.EVENT_BUS.addListener(mcjty.meecreeps.commands.ModCommands::register);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSetup.SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);
    }


    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MeeCreepsMessages.registerMessages(MODID);
            CommandHandler.registerCommands();
        });
    }


    private void enqueueImc(net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent event) {
        if (net.minecraftforge.fml.ModList.get().isLoaded("theoneprobe"))
            net.minecraftforge.fml.InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> new mcjty.meecreeps.compat.TopCompatibility());
        if (net.minecraftforge.fml.ModList.get().isLoaded("interactionwheel"))
            net.minecraftforge.fml.InterModComms.sendTo("interactionwheel", "getInteractionWheel", () -> new mcjty.meecreeps.compat.WheelCompatibility());
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
