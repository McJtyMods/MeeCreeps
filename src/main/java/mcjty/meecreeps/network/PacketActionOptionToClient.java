package mcjty.meecreeps.network;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ClientActionManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketActionOptionToClient {
    private ActionOptions options;
    private int guiid;

    public void encode(PacketBuffer buf) {
        options.writeToBuf(buf);
        buf.writeInt(guiid);
    }

    public PacketActionOptionToClient() {
    }

    public PacketActionOptionToClient(PacketBuffer buf) {
        options = new ActionOptions(buf);
        guiid = buf.readInt();
    }

    public PacketActionOptionToClient(ActionOptions options, int guiid) {
        this.options = options;
        this.guiid = guiid;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientActionManager.showActionOptions(options, guiid));
        supplier.get().setPacketHandled(true);
    }
}
