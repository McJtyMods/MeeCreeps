package mcjty.meecreeps.teleport;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.network.NetworkTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetDestination implements IMessage {

    private TeleportDestination destination;

    @Override
    public void fromBytes(ByteBuf buf) {
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), buf.readInt(),
                BlockPos.fromLong(buf.readLong()),
                EnumFacing.VALUES[buf.readByte()]);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, destination.getName());
        buf.writeInt(destination.getDimension());
        buf.writeLong(destination.getPos().toLong());
        buf.writeByte(destination.getSide().ordinal());
    }

    public PacketSetDestination() {
    }

    public PacketSetDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public static class Handler implements IMessageHandler<PacketSetDestination, IMessage> {
        @Override
        public IMessage onMessage(PacketSetDestination message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSetDestination message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return;
            PortalGunItem.addDestination(heldItem, message.destination);
        }
    }

}
