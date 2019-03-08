package mcjty.meecreeps.network;


import mcjty.lib.network.PacketHandler;
import mcjty.lib.thirteen.ChannelBuilder;
import mcjty.lib.thirteen.SimpleChannel;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.PacketActionOptionToClient;
import mcjty.meecreeps.actions.PacketPerformAction;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.teleport.PacketMakePortals;
import mcjty.meecreeps.teleport.PacketSetDestination;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class MeeCreepsMessages {

    public static SimpleNetworkWrapper INSTANCE = null;

    public MeeCreepsMessages() {
    }

    public static void registerMessages(String name) {
        SimpleChannel net = ChannelBuilder
                .named(new ResourceLocation(MeeCreeps.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net.getNetwork();

        // Server side
        net.registerMessageServer(id(), PacketPerformAction.class, PacketPerformAction::toBytes, PacketPerformAction::new, PacketPerformAction::handle);
        net.registerMessageServer(id(), PacketSetDestination.class, PacketSetDestination::toBytes, PacketSetDestination::new, PacketSetDestination::handle);
        net.registerMessageServer(id(), PacketMakePortals.class, PacketMakePortals::toBytes, PacketMakePortals::new, PacketMakePortals::handle);

        // Client side
        net.registerMessageClient(id(), PacketActionOptionToClient.class, PacketActionOptionToClient::toBytes, PacketActionOptionToClient::new, PacketActionOptionToClient::handle);
        net.registerMessageClient(id(), PacketShowBalloonToClient.class, PacketShowBalloonToClient::toBytes, PacketShowBalloonToClient::new, PacketShowBalloonToClient::handle);
    }

    private static int id() {
        return PacketHandler.nextPacketID();
    }
}
