package mcjty.meecreeps.entities;

import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.UUID;

public class EntityProjectile extends EntityThrowable {

    private TeleportDestination destination;
    private UUID playerId;

    public EntityProjectile(World worldIn) {
        super(worldIn);
    }

    public EntityProjectile(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
    }

    public EntityProjectile(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (destination != null) {
            compound.setTag("destination", destination.getCompound());
        }
        if (playerId != null) {
            compound.setUniqueId("playerId", playerId);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("destination")) {
            destination = new TeleportDestination(compound.getCompoundTag("destination"));
        } else {
            destination = null;
        }
        if (compound.hasUniqueId("playerId")) {
            playerId = compound.getUniqueId("playerId");
        } else {
            playerId = null;
        }
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                EntityPlayer player = null;
                if (playerId != null) {
                    MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
                    player = playerId == null ? null : server.getPlayerList().getPlayerByUUID(playerId);
                }
                if (player != null) {
                    TeleportationTools.makePortalPair(player, result.getBlockPos(), result.sideHit, destination);
                } else {
                    TeleportationTools.makePortalPair(world, result.getBlockPos(), result.sideHit, destination);
                }
            }
            this.setDead();
        }
    }
}