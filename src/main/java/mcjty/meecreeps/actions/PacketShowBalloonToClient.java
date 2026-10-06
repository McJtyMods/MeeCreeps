package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.lib.network.NetworkTools;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class PacketShowBalloonToClient implements CustomPacketPayload {
    public static final Type<PacketShowBalloonToClient> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("meecreeps", "packet_show_balloon_to_client"));
    public static final StreamCodec<FriendlyByteBuf, PacketShowBalloonToClient> CODEC = StreamCodec.of((buf, packet) -> packet.toBytes(buf), PacketShowBalloonToClient::new);
    @Override
    public Type<PacketShowBalloonToClient> type() { return TYPE; }

    private String message;
    private String[] parameters;

    public void fromBytes(FriendlyByteBuf buf) {
        message = NetworkTools.readStringUTF8(buf);
        int size = buf.readVarInt();
        if (size < 0 || size > 256)
            throw new IllegalArgumentException("Invalid packet collection size");
        parameters = new String[size];
        for (int i = 0; i < size; i++) {
            parameters[i] = NetworkTools.readStringUTF8(buf);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, message);
        if (parameters != null) {
            buf.writeVarInt(parameters.length);
            for (String s : parameters) {
                NetworkTools.writeStringUTF8(buf, s);
            }
        } else {
            buf.writeVarInt(0);
        }
    }

    public PacketShowBalloonToClient() {
    }

    public PacketShowBalloonToClient(FriendlyByteBuf buf) {
        fromBytes(buf);
    }

    public PacketShowBalloonToClient(String message, String... parameters) {
        this.message = message;
        this.parameters = parameters;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientActionManager.showProblem(message, parameters);
        });
    }
}
