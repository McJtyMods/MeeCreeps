package mcjty.meecreeps.network;

import mcjty.meecreeps.CommandHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;


public record PacketServerCommand(String command, int id, BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketServerCommand> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meecreeps", "command"));
    public static final StreamCodec<FriendlyByteBuf, PacketServerCommand> CODEC = StreamCodec.of(
        (buf, packet) -> {
            buf.writeUtf(packet.command, 32);
            buf.writeInt(packet.id);
            buf.writeBoolean(packet.pos != null);
            if (packet.pos != null) buf.writeBlockPos(packet.pos);
        }, buf -> new PacketServerCommand(buf.readUtf(32), buf.readInt(), buf.readBoolean() ? buf.readBlockPos() : null));
    public PacketServerCommand(String command, int id) { this(command, id, null); }
    public PacketServerCommand(String command, BlockPos pos) { this(command, -1, pos); }
    @Override public Type<PacketServerCommand> type() { return TYPE; }
    public void handle(net.minecraft.server.level.ServerPlayer sender) {
        CommandHandler.handle(sender, command, id, pos);
    }
}
