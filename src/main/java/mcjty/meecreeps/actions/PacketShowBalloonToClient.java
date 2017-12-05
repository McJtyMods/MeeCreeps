package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.network.NetworkTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketShowBalloonToClient(String message, String... parameters) {
        this.message = message;
        this.parameters = parameters;
    }

    public static class Handler implements IMessageHandler<PacketShowBalloonToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketShowBalloonToClient message, MessageContext ctx) {
            MeeCreeps.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketShowBalloonToClient message, MessageContext ctx) {
            ClientActionManager.showProblem(message.message, message.parameters);
        }
    }

}
