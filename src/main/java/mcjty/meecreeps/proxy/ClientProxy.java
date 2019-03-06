package mcjty.meecreeps.proxy;

import mcjty.lib.setup.DefaultClientProxy;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.input.KeyBindings;
import mcjty.meecreeps.input.KeyInputHandler;
import mcjty.meecreeps.items.ModItems;
import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends DefaultClientProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        MinecraftForge.EVENT_BUS.register(this);
//        OBJLoader.INSTANCE.addDomain(MeeCreeps.MODID);
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());

        // Typically initialization of models and such goes here:
        ModEntities.initModels();
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
//        ModBlocks.initItemModels();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.initModels();
        ModItems.initModels();
        ModEntities.initModels();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void renderGameOverlayEvent(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        BalloonRenderer.renderBalloon();
    }
}
