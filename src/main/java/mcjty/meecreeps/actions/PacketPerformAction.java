package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.network.NetworkTools;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPerformAction implements IMessage {

    private int id;
    private MeeCreepActionType type;
    private String furtherQuestionId;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        type = new MeeCreepActionType(NetworkTools.readStringUTF8(buf));
        furtherQuestionId = NetworkTools.readStringUTF8(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        NetworkTools.writeStringUTF8(buf, type.getId());
        NetworkTools.writeStringUTF8(buf, furtherQuestionId);
    }

    public PacketPerformAction() {
    }

    public PacketPerformAction(ActionOptions options, MeeCreepActionType type, String furtherQuestionId) {
        this.id = options.getActionId();
        this.type = type;
        this.furtherQuestionId = furtherQuestionId;
    }

    public static class Handler implements IMessageHandler<PacketPerformAction, IMessage> {
        @Override
        public IMessage onMessage(PacketPerformAction message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketPerformAction message, MessageContext ctx) {
            ServerActionManager.getManager().performAction(ctx.getServerHandler().player, message.id, message.type,
                    message.furtherQuestionId);
        }
    }

}
