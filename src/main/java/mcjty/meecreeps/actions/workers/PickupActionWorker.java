package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.entity.item.ItemEntity;
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
        List<ItemEntity> items = entity.getWorld().getEntitiesWithinAABB(ItemEntity.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.getPosX(), o1.getPosY(), o1.getPosZ(), false);
                double d2 = position.distanceSq(o2.getPosX(), o2.getPosY(), o2.getPosZ(), false);
                return Double.compare(d1, d2);
            });
            ItemEntity entityItem = items.get(0);
            helper.navigateTo(entityItem, (pos) -> helper.pickup(entityItem));
        } else if (entity.hasStuffInInventory()) {
            helper.putStuffAway();
        }
    }

}
