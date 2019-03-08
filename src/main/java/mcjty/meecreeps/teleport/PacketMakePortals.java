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

public class PacketMakePortals implements IMessage {

    private BlockPos selectedBlock;
    private TeleportDestination destination;
    private EnumFacing selectedSide;

    @Override
    public void fromBytes(ByteBuf buf) {
        selectedBlock = BlockPos.fromLong(buf.readLong());
        selectedSide = EnumFacing.VALUES[buf.readByte()];
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), buf.readInt(), BlockPos.fromLong(buf.readLong()),
                EnumFacing.VALUES[buf.readByte()]);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(selectedBlock.toLong());
        buf.writeByte(selectedSide.ordinal());
        NetworkTools.writeStringUTF8(buf, destination.getName());
        buf.writeInt(destination.getDimension());
        buf.writeLong(destination.getPos().toLong());
        buf.writeByte(destination.getSide().ordinal());
    }

    public PacketMakePortals() {
    }

    public PacketMakePortals(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketMakePortals(BlockPos selectedBlock, EnumFacing selectedSide, TeleportDestination destination) {
        this.selectedBlock = selectedBlock;
        this.selectedSide = selectedSide;
        this.destination = destination;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayerMP player = ctx.getSender();
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return; // Something went wrong

            TeleportationTools.makePortalPair(player, selectedBlock, selectedSide, destination);
        });
        ctx.setPacketHandled(true);
    }
}
