package mcjty.meecreeps.network;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ClientActionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        supplier.get().enqueueWork(() -> {
            PlayerEntity player = MeeCreeps.proxy.getClientPlayer();
            World worldIn = MeeCreeps.proxy.getClientWorld();
            BlockPos pos = options.getTargetPos();
            lastOptions = options;
            player.openGui(MeeCreeps.instance, guiid, worldIn, pos.getX(), pos.getY(), pos.getZ());

            ClientActionManager.showActionOptions(options, guiid);
        });
        supplier.get().setPacketHandled(true);
    }
}
