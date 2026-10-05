package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraftforge.network.NetworkEvent.Context;


import java.util.function.Supplier;

public class PacketShowBalloonToClient {
    private String message;
    private String[] parameters;

    public void fromBytes(FriendlyByteBuf buf) {
        message = NetworkTools.readStringUTF8(buf);
        int size = buf.readVarInt();
        if (size < 0 || size > 256)
            throw new IllegalArgumentException("Invalid packet collection size");
        parameters = new String[size];
        for (int i = 0; i < size; i++) {
            parameters[i] = NetworkTools.readStringUTF8(buf);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, message);
        if (parameters != null) {
            buf.writeVarInt(parameters.length);
            for (String s : parameters) {
                NetworkTools.writeStringUTF8(buf, s);
            }
        } else {
            buf.writeVarInt(0);
        }
    }

    public PacketShowBalloonToClient() {
    }

    public PacketShowBalloonToClient(FriendlyByteBuf buf) {
        fromBytes(buf);
    }

    public PacketShowBalloonToClient(String message, String... parameters) {
        this.message = message;
        this.parameters = parameters;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientActionManager.showProblem(message, parameters);
        });
        ctx.setPacketHandled(true);
    }
}
