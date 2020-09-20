package mcjty.meecreeps.network;

import mcjty.lib.network.NetworkTools;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPerformAction {
    private final int id;
    private final MeeCreepActionType type;
    private final String furtherQuestionId;

    public void encode(PacketBuffer buffer) {
        buffer.writeInt(id);
        NetworkTools.writeStringUTF8(buffer, type.getId());
        NetworkTools.writeStringUTF8(buffer, furtherQuestionId);
    }

    public PacketPerformAction(PacketBuffer buffer) {
        this(
                buffer.readInt(),
                new MeeCreepActionType(NetworkTools.readStringUTF8(buffer)),
                NetworkTools.readStringUTF8(buffer)
        );
    }

    public PacketPerformAction(int actionId, MeeCreepActionType type, String furtherQuestionId) {
        this.id = actionId;
        this.type = type;
        this.furtherQuestionId = furtherQuestionId;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerActionManager.getManager(context.get().getSender().world).performAction(context.get().getSender(), id, type, furtherQuestionId);
        });
        context.get().setPacketHandled(true);
    }
}
