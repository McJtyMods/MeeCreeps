package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.meecreeps.network.NetworkTools;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class PacketPerformAction implements CustomPacketPayload {
    public static final Type<PacketPerformAction> TYPE = new Type<>(Identifier.fromNamespaceAndPath("meecreeps", "packet_perform_action"));
    public static final StreamCodec<FriendlyByteBuf, PacketPerformAction> CODEC = StreamCodec.of((buf, packet) -> packet.toBytes(buf), PacketPerformAction::new);
    @Override
    public Type<PacketPerformAction> type() { return TYPE; }

    private int id;
    private MeeCreepActionType type;
    private String furtherQuestionId;

    public void fromBytes(FriendlyByteBuf buf) {
        id = buf.readInt();
        type = new MeeCreepActionType(NetworkTools.readStringUTF8(buf));
        furtherQuestionId = NetworkTools.readStringUTF8(buf);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(id);
        NetworkTools.writeStringUTF8(buf, type.getId());
        NetworkTools.writeStringUTF8(buf, furtherQuestionId);
    }

    public PacketPerformAction() {
    }

    public PacketPerformAction(FriendlyByteBuf buf) {
        fromBytes(buf);
    }

    public PacketPerformAction(ActionOptions options, MeeCreepActionType type, String furtherQuestionId) {
        this.id = options.getActionId();
        this.type = type;
        this.furtherQuestionId = furtherQuestionId;
    }

    public void handle(net.minecraft.server.level.ServerPlayer sender) {
        var player = sender;
        var manager = ServerActionManager.getManager();
        var options = manager.getOptions(id);
        if (options == null || options.getStage() != Stage.WAITING_FOR_PLAYER_INPUT)
            return;
        if (!options.getActionOptions().contains(type) && !options.getMaybeActionOptions().contains(type))
            return;
        var factory = mcjty.meecreeps.MeeCreeps.api.getFactory(type);
        if (factory == null)
            return;
        var world = mcjty.meecreeps.varia.LevelTools.getWorld(options.getDimension());
        if (world == null)
            return;
        if (furtherQuestionId != null && !factory.getFactory().getFurtherQuestions(world, options.getTargetPos(), options.getTargetSide()).stream().anyMatch(q -> java.util.Objects.equals(q.getLeft(), furtherQuestionId)))
            return;
        manager.performAction(player, id, type, furtherQuestionId);
    }
}
