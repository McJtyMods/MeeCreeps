package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.DimensionId;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class FollowAndPickupActionWorker extends AbstractActionWorker {

    public FollowAndPickupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AxisAlignedBB getActionBox() {
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
        PlayerEntity player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (!DimensionId.sameDimension(player.getEntityWorld(), meeCreep.getEntityWorld())) {
            // Wrong dimension, do nothing as this is handled by ServerActionManager
        } else {
            BlockPos position = player.getPosition();
            AxisAlignedBB box = new AxisAlignedBB(position.add(-6, -4, -6), position.add(6, 4, 6));
            List<ItemEntity> items = entity.getWorld().getEntitiesWithinAABB(ItemEntity.class, box, input -> {
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
                    double d1 = position.distanceSq(o1.getPosX(), o1.getPosY(), o1.getPosZ(), false);
                    double d2 = position.distanceSq(o2.getPosX(), o2.getPosY(), o2.getPosZ(), false);
                    return Double.compare(d1, d2);
                });
                ItemEntity entityItem = items.get(0);
                helper.navigateTo(entityItem, (pos) -> helper.pickup(entityItem));
            } else if (entity.hasStuffInInventory()) {
                helper.navigateTo(helper.findSuitablePositionNearPlayer(1.0), blockPos -> helper.giveToPlayerOrDrop());
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {});
            }
        }
    }
}
