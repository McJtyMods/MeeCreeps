package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

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

    public PacketPerformAction(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketPerformAction(ActionOptions options, MeeCreepActionType type, String furtherQuestionId) {
        this.id = options.getActionId();
        this.type = type;
        this.furtherQuestionId = furtherQuestionId;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerActionManager.getManager().performAction(ctx.getSender(), id, type, furtherQuestionId);
        });
        ctx.setPacketHandled(true);
    }
}
