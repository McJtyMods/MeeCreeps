package mcjty.meecreeps.teleport;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.items.ModItems;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.network.NetworkTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketMakePortals(BlockPos selectedBlock, EnumFacing selectedSide, TeleportDestination destination) {
        this.selectedBlock = selectedBlock;
        this.selectedSide = selectedSide;
        this.destination = destination;
    }

    public static class Handler implements IMessageHandler<PacketMakePortals, IMessage> {
        @Override
        public IMessage onMessage(PacketMakePortals message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketMakePortals message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return; // Something went wrong

            TeleportationTools.makePortalPair(player, message.selectedBlock, message.selectedSide, message.destination);
//            PortalGunItem.addDestination(heldItem, message.destination);
        }
    }

}
