package mcjty.meecreeps.network;

import mcjty.lib.network.NetworkTools;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSetDestination {
    private final TeleportDestination destination;
    private final int destinationIndex;

    public void encode(PacketBuffer buffer) {
        NetworkTools.writeStringUTF8(buffer, destination.getName());

        buffer.writeInt(destination.getDimension());
        buffer.writeLong(destination.getPos().toLong());
        buffer.writeByte(destination.getSide().ordinal());
        buffer.writeInt(destinationIndex);
    }

    public PacketSetDestination(PacketBuffer buf) {
        destination = new TeleportDestination(
                NetworkTools.readStringUTF8(buf), buf.readInt(),
                BlockPos.fromLong(buf.readLong()),
                Direction.values()[buf.readByte()]
        );
        destinationIndex = buf.readInt();
    }

    public PacketSetDestination(TeleportDestination destination, int destinationIndex) {
        this.destination = destination;
        this.destinationIndex = destinationIndex;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> {
            ServerPlayerEntity player = supplier.get().getSender();
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return;
            PortalGunItem.addDestination(heldItem, destination, destinationIndex);
        });
        supplier.get().setPacketHandled(true);
    }
}
