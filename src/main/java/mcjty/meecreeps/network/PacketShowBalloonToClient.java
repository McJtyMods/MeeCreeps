package mcjty.meecreeps.network;

import net.minecraft.network.PacketBuffer;
import mcjty.lib.network.NetworkTools;
import mcjty.meecreeps.actions.ClientActionManager;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketShowBalloonToClient {
    private String message;
    private String[] parameters;

    public void encode(PacketBuffer buf) {
        NetworkTools.writeStringUTF8(buf, message);
        if (parameters != null) {
            buf.writeInt(parameters.length);
            for (String s : parameters) {
                NetworkTools.writeStringUTF8(buf, s);
            }
        } else {
            buf.writeInt(0);
        }
    }

    public PacketShowBalloonToClient() {
    }

    public PacketShowBalloonToClient(PacketBuffer buf) {
        message = NetworkTools.readStringUTF8(buf);
        int size = buf.readInt();
        parameters = new String[size];
        for (int i = 0 ; i < size ; i++) {
            parameters[i] = NetworkTools.readStringUTF8(buf);
        }
    }

    public PacketShowBalloonToClient(String message, String... parameters) {
        this.message = message;
        this.parameters = parameters;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientActionManager.showProblem(message, parameters));
        supplier.get().setPacketHandled(true);
    }
}
