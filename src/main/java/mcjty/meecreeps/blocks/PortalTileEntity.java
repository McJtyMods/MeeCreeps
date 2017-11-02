package mcjty.meecreeps.blocks;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class PortalTileEntity extends TileEntity implements ITickable {

    private int timeout;
    private TeleportDestination other;
    private AxisAlignedBB box = null;
    private Set<UUID> blackListed = new HashSet<>();        // Entities can only go through the portal one time

    @Override
    public void update() {
        if (!world.isRemote) {
            timeout--;
            markDirty();
            if (timeout <= 0) {
                killPortal();
                getOther().ifPresent(PortalTileEntity::killPortal);
                return;
            }

            getOther().ifPresent(otherPortal -> {
                double otherX = otherPortal.getPos().getX()+.5;
                double otherY = otherPortal.getPos().getY()+.5;
                double otherZ = otherPortal.getPos().getZ()+.5;
                List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getTeleportBox());
                for (Entity entity : entities) {
                    if (!blackListed.contains(entity.getUniqueID())) {
                        otherPortal.addBlackList(entity.getUniqueID());
                        TeleportationTools.teleportEntity(entity, otherPortal.getWorld(), otherX, otherY, otherZ);
                        if (entity instanceof EntityPlayer) {
                            SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, "teleport"));
                            // @todo config
                            SoundTools.playSound(otherPortal.getWorld(), sound, otherX, otherY, otherZ, 1, 1);
                        }
                    }
                }
            });
        }
    }

    private AxisAlignedBB getTeleportBox() {
        if (box == null) {
            box = new AxisAlignedBB(pos.getX()-.2, pos.getY()-1.2, pos.getZ()-.2, pos.getX()+1.2, pos.getY()+2.2, pos.getZ()+1.2);
        }
        return box;
    }

    public void addBlackList(UUID uuid) {
        blackListed.add(uuid);
        markDirty();
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        markDirty();
    }

    public void setOther(TeleportDestination other) {
        this.other = other;
        markDirty();
    }

    public void killPortal() {
        world.setBlockToAir(getPos());
    }

    private Optional<PortalTileEntity> getOther() {
        World otherWorld = TeleportationTools.getWorldForDimension(other.getDimension());
        TileEntity te = otherWorld.getTileEntity(other.getPos().up());
        if (te instanceof PortalTileEntity) {
            return Optional.of((PortalTileEntity) te);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        timeout = compound.getInteger("timeout");
        BlockPos pos = BlockPos.fromLong(compound.getLong("pos"));
        int dim = compound.getInteger("dim");
        other = new TeleportDestination("", dim, pos);
        NBTTagList list = compound.getTagList("bl", Constants.NBT.TAG_COMPOUND);
        blackListed.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            UUID uuid = new UUID(tc.getLong("m"), tc.getLong("l"));
            blackListed.add(uuid);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("timeout", timeout);
        compound.setLong("pos", other.getPos().toLong());
        compound.setInteger("dim", other.getDimension());
        NBTTagList list = new NBTTagList();
        for (UUID uuid : blackListed) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setLong("m", uuid.getMostSignificantBits());
            tc.setLong("l", uuid.getLeastSignificantBits());
            list.appendTag(tc);
        }
        compound.setTag("bl", list);
        return super.writeToNBT(compound);
    }
}
