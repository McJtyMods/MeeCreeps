package mcjty.meecreeps.teleport;

import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TeleportationTools {

    public static void cancelPortalPair(EntityPlayer player, BlockPos selectedBlock) {
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
        if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            return true;
        }
        return false;
    }

    private static boolean canCollideWith(World world, BlockPos pos) {
        if (world.isAirBlock(pos)) {
            return false;
        }
        AxisAlignedBB box = world.getBlockState(pos).getCollisionBoundingBox(world, pos);
        return box != null;
    }

    public static void makePortalPair(EntityPlayer player, BlockPos selectedBlock, EnumFacing selectedSide, TeleportDestination dest) {
        World sourceWorld = player.getEntityWorld();
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cant_find_portal_spot"), (EntityPlayerMP) player);
            return;
        }

        World destWorld = mcjty.lib.varia.TeleportationTools.getWorldForDimension(dest.getDimension());
        if (destWorld.getBlockState(dest.getPos()).getBlock() == ModBlocks.portalBlock) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.portal_already_there"), (EntityPlayerMP) player);
            return;
        }
        if (dest.getSide() == EnumFacing.DOWN) {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().down())) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), (EntityPlayerMP) player);
                return;
            }
        } else {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().up())) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), (EntityPlayerMP) player);
                return;
            }
        }

        sourceWorld.setBlockState(sourcePortalPos, ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getTileEntity(sourcePortalPos);

        destWorld.setBlockState(dest.getPos(), ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getTileEntity(dest.getPos());

        source.setTimeout(Config.portalTimeout);
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(Config.portalTimeout);
        destination.setOther(new TeleportDestination("", sourceWorld.provider.getDimension(), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    public static void makePortalPair(World sourceWorld, BlockPos selectedBlock, EnumFacing selectedSide, TeleportDestination dest) {
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            return;
        }

        World destWorld = mcjty.lib.varia.TeleportationTools.getWorldForDimension(dest.getDimension());
        if (destWorld.getBlockState(dest.getPos()).getBlock() == ModBlocks.portalBlock) {
            return;
        }
        if (dest.getSide() == EnumFacing.DOWN) {
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

        source.setTimeout(Config.portalTimeout);
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(Config.portalTimeout);
        destination.setOther(new TeleportDestination("", sourceWorld.provider.getDimension(), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    /**
     * Return the position where the portal block should be placed
     */
    @Nullable
    public static BlockPos findBestPosition(World world, BlockPos selectedBlock, EnumFacing selectedSide) {
        if (selectedSide == EnumFacing.UP) {
            if (world.isAirBlock(selectedBlock.up()) && world.isAirBlock(selectedBlock.up(2))) {
                return selectedBlock.up();
            }
            return null;
        }
        if (selectedSide == EnumFacing.DOWN) {
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
            return findBestPosition(world, selectedBlock.down(), EnumFacing.UP);
        }
        return null;
    }


}
