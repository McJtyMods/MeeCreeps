package mcjty.meecreeps.teleport;

import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class TeleportationTools {

    public static void makePortalPair(EntityPlayer player, BlockPos selectedBlock, TeleportDestination dest) {
        World sourceWorld = player.getEntityWorld();
        sourceWorld.setBlockState(selectedBlock.up(), ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity source = (PortalTileEntity) sourceWorld.getTileEntity(selectedBlock.up());

        World destWorld = getWorldForDimension(dest.getDimension());
        destWorld.setBlockState(dest.getPos().up(),  ModBlocks.portalBlock.getDefaultState(), 3);
        PortalTileEntity destination = (PortalTileEntity) destWorld.getTileEntity(dest.getPos().up());

        source.setTimeout(30*20);
        source.setOther(dest);

        destination.setTimeout(30*20);
        destination.setOther(new TeleportDestination("", sourceWorld.provider.getDimension(), selectedBlock));
    }


    public static void performTeleport(EntityPlayer player, TeleportDestination dest) {
        BlockPos c = dest.getPos();
        int oldId = player.getEntityWorld().provider.getDimension();

        if (oldId != dest.getDimension()) {
            TeleportationTools.teleportToDimension(player, dest.getDimension(), c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5);
        } else {
            player.setPositionAndUpdate(c.getX()+0.5, c.getY()+1, c.getZ()+0.5);
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

    public static void teleportEntity(Entity entity, World destWorld, double newX, double newY, double newZ) {
        World world = entity.getEntityWorld();
        if (entity instanceof EntityPlayer) {
            if (world.provider.getDimension() != destWorld.provider.getDimension()) {
                TeleportationTools.teleportToDimension((EntityPlayer) entity, destWorld.provider.getDimension(), newX, newY, newZ);
            }
            entity.setPositionAndUpdate(newX, newY, newZ);
        } else {
            if (world.provider.getDimension() != destWorld.provider.getDimension()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                float rotationYaw = entity.rotationYaw;
                float rotationPitch = entity.rotationPitch;
                entity.writeToNBT(tagCompound);
                Class<? extends Entity> entityClass = entity.getClass();
                world.removeEntity(entity);

                try {
                    Entity newEntity = entityClass.getConstructor(World.class).newInstance(destWorld);
                    newEntity.readFromNBT(tagCompound);
                    newEntity.setLocationAndAngles(newX, newY, newZ, rotationYaw, rotationPitch);
                    destWorld.spawnEntity(newEntity);
                } catch (Exception e) {
                }
            } else {
                entity.setLocationAndAngles(newX, newY, newZ, entity.rotationYaw, entity.rotationPitch);
                destWorld.updateEntityWithOptionalForce(entity, false);
            }
        }
    }

}
