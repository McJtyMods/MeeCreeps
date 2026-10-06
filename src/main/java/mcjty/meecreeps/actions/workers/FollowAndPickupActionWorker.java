package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.List;

public class FollowAndPickupActionWorker extends AbstractActionWorker {

    public FollowAndPickupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        return null;
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public boolean needsToFollowPlayer() {
        return true;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) entity;
        Player player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (player.level().dimension() != meeCreep.level().dimension()) {
            // Wrong dimension, do nothing as this is handled by ServerActionManager
        } else {
            BlockPos position = player.blockPosition();
            AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(-6, -4, -6)), net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(6, 4, 6)));
            List<ItemEntity> items = entity.getWorld().getEntitiesOfClass(ItemEntity.class, box, input -> {
                if (!input.getItem().isEmpty()) {
                    if (input.getItem().getItem() instanceof BlockItem) {
                        if (DigTunnelActionWorker.isNotInterestedIn(((BlockItem) input.getItem().getItem()).getBlock())) {
                            return false;
                        }
                    }
                }
                return true;
            });
            if (!items.isEmpty()) {
                items.sort((o1, o2) -> {
                    double d1 = position.distToCenterSqr(o1.position());
                    double d2 = position.distToCenterSqr(o2.position());
                    return Double.compare(d1, d2);
                });
                ItemEntity entityItem = items.get(0);
                helper.navigateTo(entityItem, (pos) -> helper.pickup(entityItem));
            } else if (entity.hasStuffInInventory()) {
                helper.navigateTo(helper.findSuitablePositionNearPlayer(1.0), blockPos -> helper.giveToPlayerOrDrop());
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {
                });
            }
        }
    }
}
