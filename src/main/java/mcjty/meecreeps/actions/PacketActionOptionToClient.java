package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class PacketActionOptionToClient implements CustomPacketPayload {
    public static final Type<PacketActionOptionToClient> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meecreeps", "packet_action_option_to_client"));
    public static final StreamCodec<FriendlyByteBuf, PacketActionOptionToClient> CODEC = StreamCodec.of((buf, packet) -> packet.toBytes(buf), PacketActionOptionToClient::new);
    @Override
    public Type<PacketActionOptionToClient> type() { return TYPE; }

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

    public void handle() {
        ClientActionManager.showActionOptions(options, guiid);
    }
}
