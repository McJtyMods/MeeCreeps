package mcjty.meecreeps.teleport;

import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.varia.LevelTools;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class TeleportationTools {

    public static void cancelPortalPair(Player player, BlockPos selectedBlock) {
        Level sourceWorld = player.level();
        if (selectedBlock == null || player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(selectedBlock)) > 100)
            return;
        BlockEntity te = sourceWorld.getBlockEntity(selectedBlock);
        if (te instanceof PortalTileEntity) {
            PortalTileEntity source = (PortalTileEntity) te;
            source.setTimeout(10);
        }
    }

    private static boolean canPlacePortal(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return true;
        }
        if (world.getBlockState(pos).canBeReplaced()) {
            return true;
        }
        return false;
    }

    private static boolean canCollideWith(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return false;
        }
        return !world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
    }

    public static void makePortalPair(Player player, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest) {
        Level sourceWorld = player.level();
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cant_find_portal_spot"), (ServerPlayer) player);
            return;
        }

        Level destWorld = mcjty.meecreeps.varia.LevelTools.getWorld(dest.getDimension());
        if (destWorld == null || !destWorld.isInWorldBounds(dest.getPos()) || (destWorld == sourceWorld && dest.getPos().equals(sourcePortalPos)))
            return;
        if (destWorld.getBlockState(dest.getPos()).getBlock() == Registration.PORTAL.get()) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.portal_already_there"), (ServerPlayer) player);
            return;
        }
        if (dest.getSide() == Direction.DOWN) {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().below())) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), (ServerPlayer) player);
                return;
            }
        } else {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().above())) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.destination_obstructed"), (ServerPlayer) player);
                return;
            }
        }

        sourceWorld.setBlock(sourcePortalPos, Registration.PORTAL.get().defaultBlockState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getBlockEntity(sourcePortalPos);

        destWorld.setBlock(dest.getPos(), Registration.PORTAL.get().defaultBlockState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getBlockEntity(dest.getPos());

        source.setTimeout(ConfigSetup.portalTimeout.get());
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(ConfigSetup.portalTimeout.get());
        destination.setOther(new TeleportDestination("", sourceWorld.dimension(), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    public static void makePortalPair(Level sourceWorld, BlockPos selectedBlock, Direction selectedSide, TeleportDestination dest) {
        BlockPos sourcePortalPos = findBestPosition(sourceWorld, selectedBlock, selectedSide);
        if (sourcePortalPos == null) {
            return;
        }

        Level destWorld = mcjty.meecreeps.varia.LevelTools.getWorld(dest.getDimension());
        if (destWorld == null || !destWorld.isInWorldBounds(dest.getPos()) || (destWorld == sourceWorld && dest.getPos().equals(sourcePortalPos)))
            return;
        if (destWorld.getBlockState(dest.getPos()).getBlock() == Registration.PORTAL.get()) {
            return;
        }
        if (dest.getSide() == Direction.DOWN) {
            if (!destWorld.isEmptyBlock(dest.getPos()) || !destWorld.isEmptyBlock(dest.getPos().below())) {
                return;
            }
        } else {
            if (!destWorld.isEmptyBlock(dest.getPos()) || !destWorld.isEmptyBlock(dest.getPos().above())) {
                return;
            }
        }

        sourceWorld.setBlock(sourcePortalPos, Registration.PORTAL.get().defaultBlockState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getBlockEntity(sourcePortalPos);

        destWorld.setBlock(dest.getPos(), Registration.PORTAL.get().defaultBlockState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getBlockEntity(dest.getPos());

        source.setTimeout(ConfigSetup.portalTimeout.get());
        source.setOther(dest);
        source.setPortalSide(selectedSide);

        destination.setTimeout(ConfigSetup.portalTimeout.get());
        destination.setOther(new TeleportDestination("", sourceWorld.dimension(), sourcePortalPos, selectedSide));
        destination.setPortalSide(dest.getSide());
    }

    /**
     * Return the position where the portal block should be placed
     */
    @Nullable
    public static BlockPos findBestPosition(Level world, BlockPos selectedBlock, Direction selectedSide) {
        if (selectedSide == Direction.UP) {
            if (world.isEmptyBlock(selectedBlock.above()) && world.isEmptyBlock(selectedBlock.above(2))) {
                return selectedBlock.above();
            }
            return null;
        }
        if (selectedSide == Direction.DOWN) {
            if (world.isEmptyBlock(selectedBlock.below()) && world.isEmptyBlock(selectedBlock.below(2))) {
                return selectedBlock.below();
            }
            return null;
        }
        // Wall portals belong on the aimed block's face, even above ground.
        BlockPos portalPos = selectedBlock.relative(selectedSide);
        if (canPlacePortal(world, portalPos) && !canCollideWith(world, portalPos.above())) {
            return portalPos;
        }
        return null;
    }


}
