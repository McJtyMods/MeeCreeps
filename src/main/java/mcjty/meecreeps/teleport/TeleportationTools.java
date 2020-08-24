package mcjty.meecreeps.teleport;

import mcjty.lib.varia.DimensionId;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketShowBalloonToClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;

public class TeleportationTools {

    public static void cancelPortalPair(PlayerEntity player, BlockPos selectedBlock) {
        World sourceWorld = player.getEntityWorld();
        TileEntity te = sourceWorld.getTileEntity(selectedBlock);
        if (te instanceof PortalTileEntity) {
            PortalTileEntity source = (PortalTileEntity) te;
            source.setTimeout(10);
        }
    }

    private static boolean canPlacePortal(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return true;
        }
        if (world.getBlockState(pos).getMaterial().isReplaceable()) {
            return true;
        }
        return false;
    }

    private static boolean canCollideWith(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return false;
        }
        VoxelShape box = world.getBlockState(pos).getCollisionShape(world, pos);
        return box != VoxelShapes.empty();
    }

    public static void makePortalPair(ServerPlayerEntity player, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest) {
        World sourceWorld = player.getEntityWorld();
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cant_find_portal_spot"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            return;
        }

        World destWorld = dest.getDimension().getWorld();
        if (destWorld.getBlockState(dest.getPos()).getBlock() == ModBlocks.portalBlock) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.portal_already_there"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            return;
        }
        if (dest.getSide() == Direction.DOWN) {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().down())) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
        } else {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().up())) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
        }

        sourceWorld.setBlockState(sourcePortalPos, ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getTileEntity(sourcePortalPos);

        destWorld.setBlockState(dest.getPos(), ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getTileEntity(dest.getPos());

        source.setTimeout(ConfigSetup.portalTimeout.get());
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(ConfigSetup.portalTimeout.get());
        destination.setOther(new TeleportDestination("", DimensionId.fromWorld(sourceWorld), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    public static void makePortalPair(World sourceWorld, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest) {
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            return;
        }

        World destWorld = dest.getDimension().getWorld();
        if (destWorld.getBlockState(dest.getPos()).getBlock() == ModBlocks.portalBlock) {
            return;
        }
        if (dest.getSide() == Direction.DOWN) {
            if (!destWorld.isAirBlock(dest.getPos()) || !destWorld.isAirBlock(dest.getPos().down())) {
                return;
            }
        } else {
            if (!destWorld.isAirBlock(dest.getPos()) || !destWorld.isAirBlock(dest.getPos().up())) {
                return;
            }
        }

        sourceWorld.setBlockState(sourcePortalPos, ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getTileEntity(sourcePortalPos);

        destWorld.setBlockState(dest.getPos(), ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getTileEntity(dest.getPos());

        source.setTimeout(ConfigSetup.portalTimeout.get());
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(ConfigSetup.portalTimeout.get());
        destination.setOther(new TeleportDestination("", DimensionId.fromWorld(sourceWorld), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    /**
     * Return the position where the portal block should be placed
     */
    @Nullable
    public static BlockPos findBestPosition(World world, BlockPos selectedBlock, Direction selectedSide) {
        if (selectedSide == Direction.UP) {
            if (world.isAirBlock(selectedBlock.up()) && world.isAirBlock(selectedBlock.up(2))) {
                return selectedBlock.up();
            }
            return null;
        }
        if (selectedSide == Direction.DOWN) {
            if (world.isAirBlock(selectedBlock.down()) && world.isAirBlock(selectedBlock.down(2))) {
                return selectedBlock.down();
            }
            return null;
        }
        selectedBlock = selectedBlock.offset(selectedSide);
        if (world.isAirBlock(selectedBlock.down())) {
            selectedBlock = selectedBlock.down();
        }
        if (!world.isAirBlock(selectedBlock.down())) {
            return findBestPosition(world, selectedBlock.down(), Direction.UP);
        }
        return null;
    }


}
