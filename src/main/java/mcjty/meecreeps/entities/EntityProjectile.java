package mcjty.meecreeps.entities;

import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
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
        super(mcjty.meecreeps.setup.Registration.PROJECTILE.get(), thrower, world, new net.minecraft.world.item.ItemStack(mcjty.meecreeps.setup.Registration.PROJECTILE_ITEM.get()));
    }

    @Override
    protected net.minecraft.world.item.Item getDefaultItem() {
        return mcjty.meecreeps.setup.Registration.PROJECTILE_ITEM.get();
    }

    public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        if (destination != null) {
            compound.put("destination", destination.getCompound());
        }
        if (playerId != null) {
            compound.store("playerId", net.minecraft.core.UUIDUtil.CODEC, playerId);
        }
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        super.readAdditionalSaveData(input);
        readAdditionalSaveData(input.read("meecreeps", CompoundTag.CODEC).orElseGet(CompoundTag::new));
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        super.addAdditionalSaveData(output);
        CompoundTag data = new CompoundTag();
        addAdditionalSaveData(data);
        output.store("meecreeps", CompoundTag.CODEC, data);
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("destination")) {
            destination = new TeleportDestination(compound.getCompoundOrEmpty("destination"));
        } else {
            destination = null;
        }
        if (compound.read("playerId", net.minecraft.core.UUIDUtil.CODEC).isPresent()) {
            playerId = compound.read("playerId", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
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
        if (!level().isClientSide()) {
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
