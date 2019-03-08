package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

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

    public PacketActionOptionToClient(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketActionOptionToClient(ActionOptions options, int guiid) {
        this.options = options;
        this.guiid = guiid;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ClientActionManager.showActionOptions(options, guiid);
        });
        ctx.setPacketHandled(true);
    }
}
