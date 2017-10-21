package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPerformAction implements IMessage {

    private ActionOptions options;
    private MeeCreepActionType type;

    @Override
    public void fromBytes(ByteBuf buf) {
        options = new ActionOptions(buf);
        type = MeeCreepActionType.VALUES[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        options.writeToBuf(buf);
        buf.writeByte(type.ordinal());
    }

    public PacketPerformAction() {
    }

    public PacketPerformAction(ActionOptions options, MeeCreepActionType type) {
        this.options = options;
        this.type = type;
    }

    public static class Handler implements IMessageHandler<PacketPerformAction, IMessage> {
        @Override
        public IMessage onMessage(PacketPerformAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketPerformAction message, MessageContext ctx) {
            ServerActionManager.performAction(message.options, message.type);
        }
    }

}
