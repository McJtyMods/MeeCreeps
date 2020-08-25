package mcjty.meecreeps.entities;

import mcjty.lib.varia.DimensionId;
import mcjty.meecreeps.items.ModItems;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityProjectile extends ProjectileItemEntity {

    private TeleportDestination destination;
    private UUID playerId;

    public EntityProjectile(EntityType<EntityProjectile> type, World world) {
        super(type, world);
    }

    public EntityProjectile(World world, LivingEntity thrower) {
        super(ModEntities.PROJECTILE_ENTITY.get(), thrower, world);
    }

    public void setDestination(TeleportDestination destination) {
        this.destination = destination;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        if (destination != null) {
            compound.put("destination", destination.getCompound());
        }
        if (playerId != null) {
            compound.putUniqueId("playerId", playerId);
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        if (compound.contains("destination")) {
            destination = new TeleportDestination(compound.getCompound("destination"));
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
            if (result.getType() == RayTraceResult.Type.BLOCK) {
                BlockRayTraceResult trace = (BlockRayTraceResult) result;
                PlayerEntity player = null;
                if (playerId != null) {
                    MinecraftServer server = DimensionId.overworld().getWorld().getServer();
                    player = playerId == null ? null : server.getPlayerList().getPlayerByUUID(playerId);
                }
                if (player != null) {
                    TeleportationTools.makePortalPair((ServerPlayerEntity) player, trace.getPos(), trace.getFace(), destination);
                } else {
                    TeleportationTools.makePortalPair(world, trace.getPos(), trace.getFace(), destination);
                }
            }
            this.remove();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.PROJECTILE_ITEM.get();
    }

    @Override
    protected void registerData() {

    }
}