package mcjty.meecreeps.teleport;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.items.PortalGunItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetCurrent implements IMessage {

    private int current;

    @Override
    public void fromBytes(ByteBuf buf) {
        current = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(current);
    }

    public PacketSetCurrent() {
    }

    public PacketSetCurrent(int current) {
        this.current = current;
    }

    public static class Handler implements IMessageHandler<PacketSetCurrent, IMessage> {
        @Override
        public IMessage onMessage(PacketSetCurrent message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSetCurrent message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return; // Something went wrong

            PortalGunItem.setCurrentDestination(heldItem, message.current);
        }
    }

}
