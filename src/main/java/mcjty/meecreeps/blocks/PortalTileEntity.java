package mcjty.meecreeps.blocks;

import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.TeleportationTools;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

public class PortalTileEntity extends TileEntity implements ITickable {

    private int timeout;
    private boolean soundStart = false;
    private boolean soundEnd = false;
    private int start;  // Client side only
    private TeleportDestination other;
    private EnumFacing portalSide;            // Side to render the portal on
    private AxisAlignedBB box = null;
    private Set<UUID> blackListed = new HashSet<>();        // Entities can only go through the portal one time

    @Override
    public void update() {
        if (!world.isRemote) {
            tickTime();
            if (timeout <= 0) {
                killPortal();
                getOther().ifPresent(PortalTileEntity::killPortal);
                return;
            }

            if ((!soundStart) && timeout > ConfigSetup.portalTimeout.get()-10) {
                soundStart = true;
                SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, "portal"));
                // @todo config
                SoundTools.playSound(world, sound, pos.getX(), pos.getY(), pos.getZ(), 1, 1);
            }

            if ((!soundEnd) && timeout < 10) {
                soundEnd = true;
                if (ConfigSetup.teleportVolume.get() > 0.01f) {
                    SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, "portal"));
                    SoundTools.playSound(world, sound, pos.getX(), pos.getY(), pos.getZ(), ConfigSetup.teleportVolume.get(), 1);
                }
            }

            getOther().ifPresent(otherPortal -> {
                double otherX = otherPortal.getPos().getX()+.5;
                double otherY = otherPortal.getPos().getY()+.5;
                double otherZ = otherPortal.getPos().getZ()+.5;
                List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, getTeleportBox());
                for (Entity entity : entities) {
                    if (!blackListed.contains(entity.getUniqueID())) {
                        otherPortal.addBlackList(entity.getUniqueID());
                        double oy = otherY;
                        if (otherPortal.getPortalSide() == EnumFacing.DOWN) {
                            oy -= entity.height + .7;
                        }
                        TeleportationTools.teleportEntity(entity, otherPortal.getWorld(), otherX, oy, otherZ, otherPortal.getPortalSide());
                        setTimeout(ConfigSetup.portalTimeoutAfterEntry.get());
                        otherPortal.setTimeout(ConfigSetup.portalTimeoutAfterEntry.get());

                        if (entity instanceof EntityPlayer) {
                            if (ConfigSetup.teleportVolume.get() > 0.01f) {
                                SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, "teleport"));
                                SoundTools.playSound(otherPortal.getWorld(), sound, otherX, otherY, otherZ, ConfigSetup.teleportVolume.get(), 1);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setInteger("timeout", timeout);
        nbtTag.setInteger("start", ConfigSetup.portalTimeout.get() - timeout);
        nbtTag.setByte("portalSide", portalSide == null ? 127 : (byte) portalSide.ordinal());
        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        timeout = packet.getNbtCompound().getInteger("timeout");
        start = packet.getNbtCompound().getInteger("start");
        byte side = packet.getNbtCompound().getByte("portalSide");
        this.portalSide = side == 127 ? null : EnumFacing.VALUES[side];
    }

    private AxisAlignedBB getTeleportBox() {
        if (box == null) {
            switch (portalSide) {
                case DOWN:
                    box = new AxisAlignedBB(pos.getX() - .7, pos.getY() + .5, pos.getZ() - .7, pos.getX() + 1.7, pos.getY() + 1, pos.getZ() + 1.7);
                    break;
                case UP:
                    box = new AxisAlignedBB(pos.getX() - .7, pos.getY() - .2, pos.getZ() - .7, pos.getX() + 1.7, pos.getY() + .5, pos.getZ() + 1.7);
                    break;
                case SOUTH:
                    box = new AxisAlignedBB(pos.getX() - .2, pos.getY() - 1.2, pos.getZ() - .2, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 0.2);
                    break;
                case NORTH:
                    box = new AxisAlignedBB(pos.getX() - .2, pos.getY() - 1.2, pos.getZ() + .8, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
                case EAST:
                    box = new AxisAlignedBB(pos.getX() - .2, pos.getY() - 1.2, pos.getZ() - .2, pos.getX() + 0.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
                case WEST:
                    box = new AxisAlignedBB(pos.getX() + .8, pos.getY() - 1.2, pos.getZ() - .2, pos.getX() + 1.2, pos.getY() + 2.2, pos.getZ() + 1.2);
                    break;
            }
        }
        return box;
    }

    public void addBlackList(UUID uuid) {
        blackListed.add(uuid);
        markDirtyQuick();
    }

    public int getTimeout() {
        return timeout;
    }

    public int getStart() {
        return start;
    }

    private void markDirtyClient() {
        markDirty();
        if (getWorld() != null) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    private void markDirtyQuick() {
        if (getWorld() != null) {
            getWorld().markChunkDirty(this.pos, this);
        }
    }

    public EnumFacing getPortalSide() {
        return portalSide;
    }

    public void setPortalSide(EnumFacing portalSide) {
        this.portalSide = portalSide;
        box = null;
        markDirtyClient();
    }

    public void tickTime() {
        timeout--;
        getOther().ifPresent(otherPortal -> {
            int otherTimeout = otherPortal.getTimeout();
            if (timeout > otherTimeout) {
                timeout = otherTimeout;
            }
        });
        markDirtyClient();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
        markDirtyClient();
    }

    public void setOther(TeleportDestination other) {
        this.other = other;
        markDirtyQuick();
    }

    public void killPortal() {
        world.setBlockToAir(getPos());
    }

    private Optional<PortalTileEntity> getOther() {
        World otherWorld = TeleportationTools.getWorldForDimension(other.getDimension());
        TileEntity te = otherWorld.getTileEntity(other.getPos());
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
        byte pside = compound.getByte("portalSide");
        this.portalSide = pside == 127 ? null : EnumFacing.VALUES[pside];
        BlockPos pos = BlockPos.fromLong(compound.getLong("pos"));
        int dim = compound.getInteger("dim");
        EnumFacing side = EnumFacing.VALUES[compound.getByte("side")];
        other = new TeleportDestination("", dim, pos, side);
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
        compound.setByte("portalSide", portalSide == null ? 127 : (byte) portalSide.ordinal());
        compound.setLong("pos", other.getPos().toLong());
        compound.setInteger("dim", other.getDimension());
        compound.setByte("side", (byte) other.getSide().ordinal());
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
