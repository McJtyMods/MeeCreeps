package mcjty.meecreeps.network;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.DimensionId;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketMakePortals {
    private BlockPos selectedBlock;
    private TeleportDestination destination;
    private Direction selectedSide;

    public void encode(PacketBuffer buf) {
        buf.writeLong(selectedBlock.toLong());
        buf.writeByte(selectedSide.ordinal());
        NetworkTools.writeStringUTF8(buf, destination.getName());
//        buf.writeInt(destination.getDimension().getInternalId());
        buf.writeLong(destination.getPos().toLong());
        buf.writeByte(destination.getSide().ordinal());
    }

    public PacketMakePortals() {
    }

    public PacketMakePortals(PacketBuffer buf) {
        selectedBlock = BlockPos.fromLong(buf.readLong());
        selectedSide = Direction.values()[buf.readByte()];
        destination = new TeleportDestination(NetworkTools.readStringUTF8(buf), DimensionId.fromResourceLocation(new ResourceLocation(NetworkTools.readStringUTF8(buf))), BlockPos.fromLong(buf.readLong()),
                Direction.values()[buf.readByte()]);
    }

    public PacketMakePortals(BlockPos selectedBlock, Direction selectedSide, TeleportDestination destination) {
        this.selectedBlock = selectedBlock;
        this.selectedSide = selectedSide;
        this.destination = destination;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return; // Something went wrong

            TeleportationTools.makePortalPair(player, selectedBlock, selectedSide, destination);
        });
        context.setPacketHandled(true);
    }
}
