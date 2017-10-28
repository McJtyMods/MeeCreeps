package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketResumeAction implements IMessage {

    private int id;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public PacketResumeAction() {
    }

    public PacketResumeAction(ActionOptions options) {
        this.id = options.getActionId();
    }

    public static class Handler implements IMessageHandler<PacketResumeAction, IMessage> {
        @Override
        public IMessage onMessage(PacketResumeAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketResumeAction message, MessageContext ctx) {
            ServerActionManager.getManager().resumeAction(ctx.getServerHandler().player, message.id);
        }
    }

}
