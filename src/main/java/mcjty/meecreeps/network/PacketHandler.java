package mcjty.meecreeps.network;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    public static SimpleChannel INSTANCE = null;
    private static int ID = 0;
    public static int nextID() {
        return ID++;
    }

    public static void registerMessages(String name) {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(MeeCreeps.MODID, name), () -> "1.0", s -> true, s -> true);

        // Server side
        INSTANCE.registerMessage(nextID(),
                PacketPerformAction.class,
                PacketPerformAction::encode,
                PacketPerformAction::new,
                PacketPerformAction::handle);

        INSTANCE.registerMessage(nextID(),
                PacketSetDestination.class,
                PacketSetDestination::encode,
                PacketSetDestination::new,
                PacketSetDestination::handle);

        INSTANCE.registerMessage(nextID(),
                PacketMakePortals.class,
                PacketMakePortals::encode,
                PacketMakePortals::new,
                PacketMakePortals::handle);

        // Client side
        INSTANCE.registerMessage(nextID(),
                PacketActionOptionToClient.class,
                PacketActionOptionToClient::encode,
                PacketActionOptionToClient::new,
                PacketActionOptionToClient::handle);

        INSTANCE.registerMessage(nextID(),
                PacketShowBalloonToClient.class,
                PacketShowBalloonToClient::encode,
                PacketShowBalloonToClient::new,
                PacketShowBalloonToClient::handle);
    }
}
