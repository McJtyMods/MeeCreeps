package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.network.NetworkTools;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketShowBalloonToClient implements IMessage {
    private String message;

    @Override
    public void fromBytes(ByteBuf buf) {
        message = NetworkTools.readStringUTF8(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, message);
    }

    public PacketShowBalloonToClient() {
    }

    public PacketShowBalloonToClient(String message) {
        this.message = message;
    }

    public static class Handler implements IMessageHandler<PacketShowBalloonToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketShowBalloonToClient message, MessageContext ctx) {
            MeeCreeps.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketShowBalloonToClient message, MessageContext ctx) {
            ClientActionManager.showProblem(message.message);
        }
    }

}
