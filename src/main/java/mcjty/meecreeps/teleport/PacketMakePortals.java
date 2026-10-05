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

public class PacketMakePortals {

    private BlockPos selectedBlock;
    private TeleportDestination destination;
    private Direction selectedSide;

    public void fromBytes(FriendlyByteBuf buf) {
        selectedBlock = BlockPos.of(buf.readLong());
        selectedSide = Direction.values()[buf.readByte()];
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), buf.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION), BlockPos.of(buf.readLong()),
                Direction.values()[buf.readByte()]);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(selectedBlock.asLong());
        buf.writeByte(selectedSide.ordinal());
        NetworkTools.writeStringUTF8(buf, destination.getName());
        buf.writeResourceKey(destination.getDimension());
        buf.writeLong(destination.getPos().asLong());
        buf.writeByte(destination.getSide().ordinal());
    }

    public PacketMakePortals() {
    }

    public PacketMakePortals(FriendlyByteBuf buf) {
        fromBytes(buf);
    }

    public PacketMakePortals(BlockPos selectedBlock, Direction selectedSide, TeleportDestination destination) {
        this.selectedBlock = selectedBlock;
        this.selectedSide = selectedSide;
        this.destination = destination;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(selectedBlock)) > 100)
                return;
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty())
                return; // Something went wrong

            if (PortalGunItem.getCharge(heldItem) <= 0)
                return;
            boolean known = PortalGunItem.getDestinations(heldItem).stream().anyMatch(d -> d != null && d.getDimension() == destination.getDimension() && d.getPos().equals(destination.getPos()) && d.getSide() == destination.getSide());
            if (!known)
                return;
            PortalGunItem.setCharge(heldItem, PortalGunItem.getCharge(heldItem) - 1);
            TeleportationTools.makePortalPair(player, selectedBlock, selectedSide, destination);
        });
        ctx.setPacketHandled(true);
    }
}
