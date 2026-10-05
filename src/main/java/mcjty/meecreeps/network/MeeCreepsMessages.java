package mcjty.meecreeps.network;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.teleport.*;
import mcjty.lib.network.PacketSendServerCommand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class MeeCreepsMessages {
    private static final String PROTOCOL = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MeeCreeps.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    public static final MeeCreepsMessages INSTANCE = new MeeCreepsMessages();

    public static void registerMessages(String name) {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketPerformAction.class, PacketPerformAction::toBytes, PacketPerformAction::new, PacketPerformAction::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketSetDestination.class, PacketSetDestination::toBytes, PacketSetDestination::new, PacketSetDestination::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketMakePortals.class, PacketMakePortals::toBytes, PacketMakePortals::new, PacketMakePortals::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, PacketActionOptionToClient.class, PacketActionOptionToClient::toBytes, PacketActionOptionToClient::new, PacketActionOptionToClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, PacketShowBalloonToClient.class, PacketShowBalloonToClient::toBytes, PacketShowBalloonToClient::new, PacketShowBalloonToClient::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, PacketSendServerCommand.class, PacketSendServerCommand::write, PacketSendServerCommand::create, (packet, context) -> {
            var ctx = context.get();
            ctx.enqueueWork(() -> {
                if (ctx.getSender() != null && MeeCreeps.MODID.equals(packet.modid()))
                    mcjty.lib.McJtyLib.handleCommand(packet.modid(), packet.command(), ctx.getSender(), packet.arguments());
            });
            ctx.setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public void sendTo(Object packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
