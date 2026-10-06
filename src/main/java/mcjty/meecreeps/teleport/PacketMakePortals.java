package mcjty.meecreeps.teleport;

import net.minecraft.network.FriendlyByteBuf;
import mcjty.lib.network.NetworkTools;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import mcjty.meecreeps.items.PortalGunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class PacketMakePortals implements CustomPacketPayload {
    public static final Type<PacketMakePortals> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("meecreeps", "packet_make_portals"));
    public static final StreamCodec<FriendlyByteBuf, PacketMakePortals> CODEC = StreamCodec.of((buf, packet) -> packet.toBytes(buf), PacketMakePortals::new);
    @Override
    public Type<PacketMakePortals> type() { return TYPE; }

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

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
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
    }
}
