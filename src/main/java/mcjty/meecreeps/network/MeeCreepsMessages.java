package mcjty.meecreeps.network;


import mcjty.lib.network.PacketHandler;
import mcjty.meecreeps.actions.PacketActionOptionToClient;
import mcjty.meecreeps.actions.PacketPerformAction;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.teleport.PacketMakePortals;
import mcjty.meecreeps.teleport.PacketSetDestination;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MeeCreepsMessages {

    public static SimpleNetworkWrapper INSTANCE = null;

    public MeeCreepsMessages() {
    }

    public static void registerMessages(SimpleNetworkWrapper net) {
        INSTANCE = net;
        registerMessages();
    }

    public static void registerMessages() {
        // Server side
        INSTANCE.registerMessage(PacketPerformAction.Handler.class, PacketPerformAction.class, PacketHandler.nextPacketID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetDestination.Handler.class, PacketSetDestination.class, PacketHandler.nextPacketID(), Side.SERVER);
        INSTANCE.registerMessage(PacketMakePortals.Handler.class, PacketMakePortals.class, PacketHandler.nextPacketID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketActionOptionToClient.Handler.class, PacketActionOptionToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketShowBalloonToClient.Handler.class, PacketShowBalloonToClient.class, PacketHandler.nextPacketID(), Side.CLIENT);
    }
}
