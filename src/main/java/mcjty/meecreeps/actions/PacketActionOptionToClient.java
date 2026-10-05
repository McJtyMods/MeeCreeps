package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;


import java.util.function.Supplier;

public class PacketActionOptionToClient {
    private ActionOptions options;
    private int guiid;

    public void fromBytes(FriendlyByteBuf buf) {
        options = new ActionOptions(buf);
        guiid = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        options.writeToBuf(buf);
        buf.writeInt(guiid);
    }

    public PacketActionOptionToClient() {
    }

    public PacketActionOptionToClient(FriendlyByteBuf buf) {
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
