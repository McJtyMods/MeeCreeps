package mcjty.meecreeps.network;


import mcjty.meecreeps.actions.PacketActionOptionToClient;
import mcjty.meecreeps.actions.PacketCancelAction;
import mcjty.meecreeps.actions.PacketPerformAction;
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

        // Client side
        INSTANCE.registerMessage(PacketActionOptionToClient.Handler.class, PacketActionOptionToClient.class, nextID(), Side.CLIENT);
    }
}
