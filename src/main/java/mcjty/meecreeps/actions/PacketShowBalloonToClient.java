package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketShowBalloonToClient implements IMessage {
    private String message;
    private String[] parameters;

    @Override
    public void fromBytes(ByteBuf buf) {
        message = NetworkTools.readStringUTF8(buf);
        int size = buf.readInt();
        parameters = new String[size];
        for (int i = 0 ; i < size ; i++) {
            parameters[i] = NetworkTools.readStringUTF8(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
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

    public PacketShowBalloonToClient(ByteBuf buf) {
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
