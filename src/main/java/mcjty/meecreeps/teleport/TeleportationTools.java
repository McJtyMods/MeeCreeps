package mcjty.meecreeps.teleport;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class TeleportationTools {

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
    public static World getWorldForDimension(World world, int id) {
        World w = DimensionManager.getWorld(id);
        if (w == null) {
            w = world.getMinecraftServer().getWorld(id);
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
}
