package mcjty.meecreeps.setup;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.entities.*;
import mcjty.meecreeps.blocks.PortalTESR;
import mcjty.meecreeps.gui.GuiWheel;
import mcjty.meecreeps.input.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.*;

@Mod.EventBusSubscriber(modid = MeeCreeps.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(Registration.CREEP.get(), RenderMeeCreeps::new);
        e.registerEntityRenderer(Registration.PROJECTILE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        e.registerBlockEntityRenderer(Registration.PORTAL_TILE.get(), PortalTESR::new);
    }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(MeeCreepsModel.LAYER, MeeCreepsModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(KeyBindings.REPEAT);
    }

    public static void openWheel(BlockPos pos, Direction side) {
        GuiWheel.selectedBlock = pos;
        GuiWheel.selectedSide = side;
        Minecraft.getInstance().setScreen(new GuiWheel());
    }
}
