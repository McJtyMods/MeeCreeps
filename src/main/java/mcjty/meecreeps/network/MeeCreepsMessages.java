package mcjty.meecreeps.network;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.teleport.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.*;

public final class MeeCreepsMessages {
    public static final MeeCreepsMessages INSTANCE = new MeeCreepsMessages();
    public static void registerMessages() {
        PayloadTypeRegistry.serverboundPlay().register(PacketPerformAction.TYPE, PacketPerformAction.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketSetDestination.TYPE, PacketSetDestination.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketMakePortals.TYPE, PacketMakePortals.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PacketServerCommand.TYPE, PacketServerCommand.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketActionOptionToClient.TYPE, PacketActionOptionToClient.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PacketShowBalloonToClient.TYPE, PacketShowBalloonToClient.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PacketPerformAction.TYPE, (packet, ctx) -> packet.handle(ctx.player()));
        ServerPlayNetworking.registerGlobalReceiver(PacketSetDestination.TYPE, (packet, ctx) -> packet.handle(ctx.player()));
        ServerPlayNetworking.registerGlobalReceiver(PacketMakePortals.TYPE, (packet, ctx) -> packet.handle(ctx.player()));
        ServerPlayNetworking.registerGlobalReceiver(PacketServerCommand.TYPE, (packet, ctx) -> packet.handle(ctx.player()));
    }
    public void sendTo(CustomPacketPayload packet, ServerPlayer player) { ServerPlayNetworking.send(player, packet); }
    public void sendToServer(CustomPacketPayload packet) { net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(packet); }
}
