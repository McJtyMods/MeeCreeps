package mcjty.meecreeps.varia;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;

public final class EntityTeleportation {
    public static Entity teleportEntity(Entity entity, Level destination, double x, double y, double z, Direction side) {
        if (!(destination instanceof ServerLevel target))
            return entity;
        float yaw = side.getAxis().isHorizontal() ? side.toYRot() : entity.getYRot();
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(target, x, y, z, yaw, player.getXRot());
            return player;
        }
        if (entity.level() == target) {
            entity.moveTo(x, y, z, yaw, entity.getXRot());
            return entity;
        }
        return entity.changeDimension(new DimensionTransition(target, new net.minecraft.world.phys.Vec3(x, y, z), entity.getDeltaMovement(), yaw, entity.getXRot(), DimensionTransition.DO_NOTHING));
    }
}
