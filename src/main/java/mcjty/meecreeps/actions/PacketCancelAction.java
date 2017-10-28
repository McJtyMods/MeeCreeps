package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCancelAction implements IMessage {

    private int id;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    public PacketCancelAction() {
    }

    public PacketCancelAction(ActionOptions options) {
        this.id = options.getActionId();
    }

    public static class Handler implements IMessageHandler<PacketCancelAction, IMessage> {
        @Override
        public IMessage onMessage(PacketCancelAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCancelAction message, MessageContext ctx) {
            ServerActionManager.getManager().cancelAction(ctx.getServerHandler().player, message.id);
        }
    }

}
