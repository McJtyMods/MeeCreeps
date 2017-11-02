package mcjty.meecreeps.teleport;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.items.PortalGunItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCancelPortal implements IMessage {

    private BlockPos selectedBlock;

    @Override
    public void fromBytes(ByteBuf buf) {
        selectedBlock = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(selectedBlock.toLong());
    }

    public PacketCancelPortal() {
    }

    public PacketCancelPortal(BlockPos selectedBlock) {
        this.selectedBlock = selectedBlock;
    }

    public static class Handler implements IMessageHandler<PacketCancelPortal, IMessage> {
        @Override
        public IMessage onMessage(PacketCancelPortal message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketCancelPortal message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return; // Something went wrong

            TeleportationTools.cancelPortalPair(player, message.selectedBlock);
        }
    }

}
