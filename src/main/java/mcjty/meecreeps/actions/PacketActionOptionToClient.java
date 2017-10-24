package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.MeeCreeps;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketActionOptionToClient implements IMessage {
    private ActionOptions options;
    private int guiid;

    @Override
    public void fromBytes(ByteBuf buf) {
        options = new ActionOptions(buf);
        guiid = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        options.writeToBuf(buf);
        buf.writeInt(guiid);
    }

    public PacketActionOptionToClient() {
    }

    public PacketActionOptionToClient(ActionOptions options, int guiid) {
        this.options = options;
        this.guiid = guiid;
    }

    public static class Handler implements IMessageHandler<PacketActionOptionToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketActionOptionToClient message, MessageContext ctx) {
            MeeCreeps.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketActionOptionToClient message, MessageContext ctx) {
            ClientActionManager.showActionOptions(message.options, message.guiid);
        }
    }

}
