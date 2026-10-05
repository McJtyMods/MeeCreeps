package mcjty.meecreeps.teleport;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraftforge.network.NetworkEvent.Context;
import mcjty.meecreeps.items.PortalGunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;


import java.util.function.Supplier;

public class PacketSetDestination {

    private TeleportDestination destination;
    private int destinationIndex;

    public void fromBytes(FriendlyByteBuf buf) {
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), buf.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION),
                BlockPos.of(buf.readLong()),
                Direction.values()[buf.readByte()]);
        destinationIndex = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, destination.getName());
        buf.writeResourceKey(destination.getDimension());
        buf.writeLong(destination.getPos().asLong());
        buf.writeByte(destination.getSide().ordinal());
        buf.writeInt(destinationIndex);
    }

    public PacketSetDestination() {
    }

    public PacketSetDestination(FriendlyByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSetDestination(TeleportDestination destination, int destinationIndex) {
        this.destination = destination;
        this.destinationIndex = destinationIndex;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || destinationIndex < 0 || destinationIndex >= 8 || destination.getName().length() > 64)
                return;
            if (destination.getDimension() != player.level().dimension() || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(destination.getPos())) > 100)
                return;
            if (!player.level().hasChunkAt(destination.getPos()) || !player.level().getBlockState(destination.getPos()).canBeReplaced())
                return;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty())
                return;
            PortalGunItem.addDestination(heldItem, destination, destinationIndex);
        });
        ctx.setPacketHandled(true);
    }
}
