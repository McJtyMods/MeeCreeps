package mcjty.meecreeps.network;


import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.teleport.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    private static int ID = 12;
    private static int packetId = 0;

    public static SimpleNetworkWrapper INSTANCE = null;

    public static int nextPacketID() {
        return packetId++;
    }

    public PacketHandler() {
    }

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
        registerMessages();
    }

    public static void registerMessages() {
        // Server side
        INSTANCE.registerMessage(PacketPerformAction.Handler.class, PacketPerformAction.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCancelAction.Handler.class, PacketCancelAction.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketResumeAction.Handler.class, PacketResumeAction.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetDestination.Handler.class, PacketSetDestination.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketMakePortals.Handler.class, PacketMakePortals.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketCancelPortal.Handler.class, PacketCancelPortal.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketSetCurrent.Handler.class, PacketSetCurrent.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(PacketDeleteDestination.Handler.class, PacketDeleteDestination.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketActionOptionToClient.Handler.class, PacketActionOptionToClient.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(PacketShowBalloonToClient.Handler.class, PacketShowBalloonToClient.class, nextID(), Side.CLIENT);
    }
}
