package mcjty.meecreeps.blocks;

import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.varia.LevelTools;
import mcjty.meecreeps.varia.SoundTools;
import mcjty.meecreeps.varia.EntityTeleportation;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;

import java.util.*;

public class PortalTileEntity extends BlockEntity {

    public PortalTileEntity(BlockPos pos, BlockState state) {
        super(mcjty.meecreeps.setup.Registration.PORTAL_TILE.get(), pos, state);
    }

    private int timeout;
    private boolean soundStart = false;
    private boolean soundEnd = false;
    private int start;  // Client side only
    private TeleportDestination other;
    private Direction portalSide;            // Side to render the portal on
    private AABB box = null;
    private Set<UUID> blackListed = new HashSet<>();        // Entities can only go through the portal one time

    public void update() {
        if (level != null && !level.isClientSide() && other != null && portalSide != null) {
            tickTime();
            if (timeout <= 0) {
                killPortal();
                getOther().ifPresent(PortalTileEntity::killPortal);
                return;
            }

            if ((!soundStart) && timeout > ConfigSetup.portalTimeout.get() - 10) {
                soundStart = true;
                SoundEvent sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "portal"));
                // @todo config
                SoundTools.playSound(level, sound, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1, 1);
            }

            if ((!soundEnd) && timeout < 10) {
                soundEnd = true;
                if (ConfigSetup.teleportVolume.get() > 0.01f) {
                    SoundEvent sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "portal"));
                    SoundTools.playSound(level, sound, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), ConfigSetup.teleportVolume.get(), 1);
                }
            }

            getOther().ifPresent(otherPortal -> {
                double otherX = otherPortal.getBlockPos().getX() + .5;
                double otherY = otherPortal.getBlockPos().getY() + .5;
                double otherZ = otherPortal.getBlockPos().getZ() + .5;
                List<Entity> entities = level.getEntitiesOfClass(Entity.class, getTeleportBox());
                for (Entity entity : entities) {
                    if (!blackListed.contains(entity.getUUID())) {
                        otherPortal.addBlackList(entity.getUUID());
                        double oy = otherY;
                        if (otherPortal.getPortalSide() == Direction.DOWN) {
                            oy -= entity.getBbHeight() + .7;
                        }
                        EntityTeleportation.teleportEntity(entity, otherPortal.getLevel(), otherX, oy, otherZ, otherPortal.getPortalSide());
                        setTimeout(ConfigSetup.portalTimeoutAfterEntry.get());
                        otherPortal.setTimeout(ConfigSetup.portalTimeoutAfterEntry.get());

                        if (entity instanceof Player) {
                            if (ConfigSetup.teleportVolume.get() > 0.01f) {
                                SoundEvent sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getValue(Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "teleport"));
                                SoundTools.playSound(otherPortal.getLevel(), sound, otherX, otherY, otherZ, ConfigSetup.teleportVolume.get(), 1);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private AABB getTeleportBox() {
        if (box == null) {
            switch (portalSide) {
                case DOWN:
                    box = new AABB(worldPosition.getX() - .7, worldPosition.getY() + .5, worldPosition.getZ() - .7, worldPosition.getX() + 1.7, worldPosition.getY() + 1, worldPosition.getZ() + 1.7);
                    break;
                case UP:
                    box = new AABB(worldPosition.getX() - .7, worldPosition.getY() - .2, worldPosition.getZ() - .7, worldPosition.getX() + 1.7, worldPosition.getY() + .5, worldPosition.getZ() + 1.7);
                    break;
                case SOUTH:
                    box = new AABB(worldPosition.getX() - .2, worldPosition.getY() - 1.2, worldPosition.getZ() - .2, worldPosition.getX() + 1.2, worldPosition.getY() + 2.2, worldPosition.getZ() + 0.2);
                    break;
                case NORTH:
                    box = new AABB(worldPosition.getX() - .2, worldPosition.getY() - 1.2, worldPosition.getZ() + .8, worldPosition.getX() + 1.2, worldPosition.getY() + 2.2, worldPosition.getZ() + 1.2);
                    break;
                case EAST:
                    box = new AABB(worldPosition.getX() - .2, worldPosition.getY() - 1.2, worldPosition.getZ() - .2, worldPosition.getX() + 0.2, worldPosition.getY() + 2.2, worldPosition.getZ() + 1.2);
                    break;
                case WEST:
                    box = new AABB(worldPosition.getX() + .8, worldPosition.getY() - 1.2, worldPosition.getZ() - .2, worldPosition.getX() + 1.2, worldPosition.getY() + 2.2, worldPosition.getZ() + 1.2);
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
        setChanged();
        if (getLevel() != null) {
            BlockState state = getLevel().getBlockState(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    private void markDirtyQuick() {
        if (getLevel() != null) {
            setChanged();
        }
    }

    public Direction getPortalSide() {
        return portalSide;
    }

    public void setPortalSide(Direction portalSide) {
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
        level.removeBlock(getBlockPos(), false);
    }

    private Optional<PortalTileEntity> getOther() {
        if (other == null)
            return Optional.empty();
        Level otherWorld = LevelTools.getWorld(other.getDimension());
        if (otherWorld == null)
            return Optional.empty();
        BlockEntity te = otherWorld.getBlockEntity(other.getPos());
        if (te instanceof PortalTileEntity) {
            return Optional.of((PortalTileEntity) te);
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        loadAdditional(input.read("meecreeps", CompoundTag.CODEC).orElseGet(CompoundTag::new), level == null ? net.minecraft.core.RegistryAccess.EMPTY : level.registryAccess());
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag data = new CompoundTag();
        saveAdditional(data, level == null ? net.minecraft.core.RegistryAccess.EMPTY : level.registryAccess());
        output.store("meecreeps", CompoundTag.CODEC, data);
    }

    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        timeout = tag.getIntOr("timeout", 0);
        start = tag.getIntOr("start", 0);
        portalSide = Direction.from3DDataValue(tag.getByteOr("portalSide", (byte) 0));
        other = tag.contains("other") ? new TeleportDestination(tag.getCompoundOrEmpty("other")) : null;
        blackListed.clear();
        ListTag list = tag.getListOrEmpty("bl");
        for (int i = 0; i < list.size(); i++)
            blackListed.add(list.getCompoundOrEmpty(i).read("id", net.minecraft.core.UUIDUtil.CODEC).orElse(null));
        box = null;
    }

    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putInt("timeout", timeout);
        tag.putInt("start", ConfigSetup.portalTimeout.get() - timeout);
        tag.putByte("portalSide", (byte) (portalSide == null ? Direction.UP.ordinal() : portalSide.ordinal()));
        if (other != null)
            tag.put("other", other.getCompound());
        ListTag list = new ListTag();
        for (UUID id : blackListed) {
            CompoundTag t = new CompoundTag();
            t.store("id", net.minecraft.core.UUIDUtil.CODEC, id);
            list.add(t);
        }
        tag.put("bl", list);
    }
}
