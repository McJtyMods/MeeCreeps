package mcjty.meecreeps.teleport;

import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

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
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("Can't find good spot for portal!"), (EntityPlayerMP) player);
            return;
        }

        World destWorld = getWorldForDimension(dest.getDimension());
        if (destWorld.getBlockState(dest.getPos()).getBlock() == ModBlocks.portalBlock) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("There is already a portal at the destination!"), (EntityPlayerMP) player);
            return;
        }
        if (dest.getSide() == EnumFacing.DOWN) {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().down())) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("The destination seems obstructed!"), (EntityPlayerMP) player);
                return;
            }
        } else {
            if (!canPlacePortal(destWorld, dest.getPos()) || canCollideWith(destWorld, dest.getPos().up())) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("The destination seems obstructed!"), (EntityPlayerMP) player);
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

        World destWorld = getWorldForDimension(dest.getDimension());
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


    public static void performTeleport(EntityPlayer player, TeleportDestination dest) {
        BlockPos c = dest.getPos();
        int oldId = player.getEntityWorld().provider.getDimension();

        if (oldId != dest.getDimension()) {
            TeleportationTools.teleportToDimension(player, dest.getDimension(), c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5);
        } else {
            player.setPositionAndUpdate(c.getX() + 0.5, c.getY() + 1, c.getZ() + 0.5);
        }

//        if (TeleportConfiguration.whooshMessage) {
//            Logging.message(player, "Whoosh!");
//        }

//            if (TeleportConfiguration.teleportVolume >= 0.01) {
//                SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(RFTools.MODID, "teleport_whoosh"));
//                if (sound == null) {
//                    throw new RuntimeException("Could not find sound 'teleport_whoosh'!");
//                } else {
//                    SoundTools.playSound(player.getEntityWorld(), sound, player.posX, player.posY, player.posZ, TeleportConfiguration.teleportVolume, 1.0f);
//                }
//            }
    }

    /**
     * Get a world for a dimension, possibly loading it from the configuration manager.
     */
    public static World getWorldForDimension(int id) {
        World w = DimensionManager.getWorld(id);
        if (w == null) {
            w = DimensionManager.getWorld(0).getMinecraftServer().getWorld(id);
        }
        return w;
    }


    public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z) {
        int oldDimension = player.getEntityWorld().provider.getDimension();
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
        MinecraftServer server = player.getEntityWorld().getMinecraftServer();
        WorldServer worldServer = server.getWorld(dimension);
        player.addExperienceLevel(0);


        worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new MeeCreepsTeleporter(worldServer, x, y, z));
        player.setPositionAndUpdate(x, y, z);
        if (oldDimension == 1) {
            // For some reason teleporting out of the end does weird things.
            player.setPositionAndUpdate(x, y, z);
            worldServer.spawnEntity(player);
            worldServer.updateEntityWithOptionalForce(player, false);
        }
    }

    private static void facePosition(Entity entity, double newX, double newY, double newZ, BlockPos dest) {
        double d0 = dest.getX() - newX;
        double d1 = dest.getY() - (newY + entity.getEyeHeight());
        double d2 = dest.getZ() - newZ;

        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
        float f1 = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
        entity.rotationPitch = updateRotation(entity.rotationPitch, f1);
        entity.rotationYaw = updateRotation(entity.rotationYaw, f);
    }

    private static float updateRotation(float angle, float targetAngle) {
        float f = MathHelper.wrapDegrees(targetAngle - angle);
        return angle + f;
    }


    public static void teleportEntity(Entity entity, World destWorld, double newX, double newY, double newZ, EnumFacing facing) {
        World world = entity.getEntityWorld();
        if (entity instanceof EntityPlayer) {
            if (world.provider.getDimension() != destWorld.provider.getDimension()) {
                TeleportationTools.teleportToDimension((EntityPlayer) entity, destWorld.provider.getDimension(), newX, newY, newZ);
            }

            fixOrientation(entity, newX, newY, newZ, facing);
            entity.setPositionAndUpdate(newX, newY, newZ);
        } else {
            if (world.provider.getDimension() != destWorld.provider.getDimension()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.removeTag("Dimension");
                entity.writeToNBT(tagCompound);
                Class<? extends Entity> entityClass = entity.getClass();
                world.removeEntity(entity);

                try {
                    Entity newEntity = entityClass.getConstructor(World.class).newInstance(destWorld);
                    newEntity.readFromNBT(tagCompound);
                    fixOrientation(entity, newX, newY, newZ, facing);
                    newEntity.setLocationAndAngles(newX, newY, newZ, entity.rotationYaw, entity.rotationPitch);
                    destWorld.spawnEntity(newEntity);
                } catch (Exception e) {
                }
            } else {
                fixOrientation(entity, newX, newY, newZ, facing);
                entity.setLocationAndAngles(newX, newY, newZ, entity.rotationYaw, entity.rotationPitch);
                destWorld.updateEntityWithOptionalForce(entity, false);
            }
        }
    }

    private static void fixOrientation(Entity entity, double newX, double newY, double newZ, EnumFacing facing) {
        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
            facePosition(entity, newX, newY, newZ, new BlockPos(newX, newY, newZ).offset(facing, 4));
        }
    }

}
