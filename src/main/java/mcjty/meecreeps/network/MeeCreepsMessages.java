package mcjty.meecreeps.network;

import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.teleport.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MeeCreepsMessages {
    public static final MeeCreepsMessages INSTANCE = new MeeCreepsMessages();
    public static void registerMessages(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("4");
        registrar.playToServer(PacketPerformAction.TYPE, PacketPerformAction.CODEC, (packet, ctx) -> packet.handle(ctx));
        registrar.playToServer(PacketSetDestination.TYPE, PacketSetDestination.CODEC, (packet, ctx) -> packet.handle(ctx));
        registrar.playToServer(PacketMakePortals.TYPE, PacketMakePortals.CODEC, (packet, ctx) -> packet.handle(ctx));
        registrar.playToClient(PacketActionOptionToClient.TYPE, PacketActionOptionToClient.CODEC, (packet, ctx) -> packet.handle(ctx));
        registrar.playToClient(PacketShowBalloonToClient.TYPE, PacketShowBalloonToClient.CODEC, (packet, ctx) -> packet.handle(ctx));
        registrar.playToServer(PacketServerCommand.TYPE, PacketServerCommand.CODEC, (packet, ctx) -> packet.handle(ctx));
    }
    public void sendTo(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
    public void sendToServer(CustomPacketPayload packet) {
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(packet);
    }
}
