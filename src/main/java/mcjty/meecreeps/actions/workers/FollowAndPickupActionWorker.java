package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
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
        EntityPlayer player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension()) {
            // Wrong dimension, do nothing as this is handled by ServerActionManager
        } else {
            BlockPos position = player.getPosition();
            AxisAlignedBB box = new AxisAlignedBB(position.add(-6, -4, -6), position.add(6, 4, 6));
            List<EntityItem> items = entity.getWorld().getEntitiesWithinAABB(EntityItem.class, box, input -> {
                if (!input.getItem().isEmpty()) {
                    if (input.getItem().getItem() instanceof ItemBlock) {
                        if (DigTunnelActionWorker.isNotInterestedIn(((ItemBlock) input.getItem().getItem()).getBlock())) {
                            return false;
                        }
                    }
                }
                return true;
            });
            if (!items.isEmpty()) {
                items.sort((o1, o2) -> {
                    double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                    double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                    return Double.compare(d1, d2);
                });
                EntityItem entityItem = items.get(0);
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
