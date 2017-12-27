package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PickupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    public PickupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -10, -10), options.getTargetPos().add(10, 10, 10));
        }
        return actionBox;
    }


    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
        } else {
            tryFindingItemsToPickup();
        }
    }

    private void tryFindingItemsToPickup() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos position = entity.getEntity().getPosition();
        List<EntityItem> items = entity.getWorld().getEntitiesWithinAABB(EntityItem.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            helper.navigateTo(entityItem, (pos) -> helper.pickup(entityItem));
        } else if (entity.hasStuffInInventory()) {
            helper.putStuffAway();
        }
    }

}
