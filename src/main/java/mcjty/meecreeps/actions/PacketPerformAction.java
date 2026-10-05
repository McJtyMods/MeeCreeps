package mcjty.meecreeps.actions;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraftforge.network.NetworkEvent.Context;


import java.util.function.Supplier;

public class PacketPerformAction {

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

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() == null)
                return;
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
            manager.performAction(ctx.getSender(), id, type, furtherQuestionId);
        });
        ctx.setPacketHandled(true);
    }
}
