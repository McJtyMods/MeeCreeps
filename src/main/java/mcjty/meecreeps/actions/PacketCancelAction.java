package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCancelAction implements IMessage {

    private ActionOptions options;

    @Override
    public void fromBytes(ByteBuf buf) {
        options = new ActionOptions(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        options.writeToBuf(buf);
    }

    public PacketCancelAction() {
    }

    public PacketCancelAction(ActionOptions options) {
        this.options = options;
    }

    public static class Handler implements IMessageHandler<PacketCancelAction, IMessage> {
        @Override
        public IMessage onMessage(PacketCancelAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCancelAction message, MessageContext ctx) {
            ServerActionManager.getManager().cancelAction(message.options);
        }
    }

}
