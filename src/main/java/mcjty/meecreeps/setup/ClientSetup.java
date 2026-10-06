package mcjty.meecreeps.setup;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.entities.*;
import mcjty.meecreeps.blocks.PortalTESR;
import mcjty.meecreeps.gui.GuiWheel;
import mcjty.meecreeps.input.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;

public final class ClientSetup implements net.fabricmc.api.ClientModInitializer {
    @Override public void onInitializeClient() {
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(Registration.CREEP.get(), RenderMeeCreeps::new);
        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(Registration.PROJECTILE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(Registration.PORTAL_TILE.get(), PortalTESR::new);
        net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry.registerModelLayer(MeeCreepsModel.LAYER, MeeCreepsModel::createBodyLayer);
        net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(KeyBindings.REPEAT);
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            mcjty.meecreeps.render.BalloonRenderer.tick();
            mcjty.meecreeps.input.KeyInputHandler.key();
        });
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(net.minecraft.resources.Identifier.parse("meecreeps:balloon"), (graphics, delta) -> mcjty.meecreeps.render.BalloonRenderer.render(graphics));
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mcjty.meecreeps.actions.PacketActionOptionToClient.TYPE, (packet, ctx) -> packet.handle());
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(mcjty.meecreeps.actions.PacketShowBalloonToClient.TYPE, (packet, ctx) -> packet.handle());
    }

    public static void openWheel(BlockPos pos, Direction side) {
        GuiWheel.selectedBlock = pos;
        GuiWheel.selectedSide = side;
        Minecraft.getInstance().gui.setScreen(new GuiWheel());
    }
}
