package mcjty.meecreeps.entities;

import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import mcjty.meecreeps.setup.Registration;

import java.util.UUID;

public class EntityProjectile extends ThrowableItemProjectile {

    private TeleportDestination destination;
    private UUID playerId;

    public EntityProjectile(net.minecraft.world.entity.EntityType<? extends EntityProjectile> type, Level world) {
        super(type, world);
    }

    public EntityProjectile(Level world, LivingEntity thrower) {
        super(Registration.PROJECTILE.get(), thrower, world);
    }

    @Override
    protected net.minecraft.world.item.Item getDefaultItem() {
        return Registration.PROJECTILE_ITEM.get();
    }

    public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (destination != null) {
            compound.put("destination", destination.getCompound());
        }
        if (playerId != null) {
            compound.putUUID("playerId", playerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("destination")) {
            destination = new TeleportDestination(compound.getCompound("destination"));
        } else {
            destination = null;
        }
        if (compound.hasUUID("playerId")) {
            playerId = compound.getUUID("playerId");
        } else {
            playerId = null;
        }
    }

    /**
     * Called when this ThrowableItemProjectile hits a block or entity.
     */
    @Override
    protected void onHit(net.minecraft.world.phys.HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) {
            if (hit instanceof BlockHitResult result && destination != null) {
                Player player = playerId == null ? null : level().getServer().getPlayerList().getPlayer(playerId);
                if (player != null)
                    TeleportationTools.makePortalPair(player, result.getBlockPos(), result.getDirection(), destination);
                else
                    TeleportationTools.makePortalPair(level(), result.getBlockPos(), result.getDirection(), destination);
            }
            discard();
        }
    }
}
