package mcjty.meecreeps.teleport;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.meecreeps.items.PortalGunItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketSetDestination implements IMessage {

    private TeleportDestination destination;
    private int destinationIndex;

    @Override
    public void fromBytes(ByteBuf buf) {
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), buf.readInt(),
                BlockPos.fromLong(buf.readLong()),
                EnumFacing.VALUES[buf.readByte()]);
        destinationIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, destination.getName());
        buf.writeInt(destination.getDimension());
        buf.writeLong(destination.getPos().toLong());
        buf.writeByte(destination.getSide().ordinal());
        buf.writeInt(destinationIndex);
    }

    public PacketSetDestination() {
    }

    public PacketSetDestination(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSetDestination(TeleportDestination destination, int destinationIndex) {
        this.destination = destination;
        this.destinationIndex = destinationIndex;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return;
            PortalGunItem.addDestination(heldItem, destination, destinationIndex);
        });
        ctx.setPacketHandled(true);
    }
}
