package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.List;

public class PickupActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;

    public PickupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(-10, -10, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(10, 10, 10)));
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
        BlockPos position = entity.getEntity().blockPosition();
        List<ItemEntity> items = entity.getWorld().getEntitiesOfClass(ItemEntity.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distToCenterSqr(o1.position());
                double d2 = position.distToCenterSqr(o2.position());
                return Double.compare(d1, d2);
            });
            ItemEntity entityItem = items.get(0);
            helper.navigateTo(entityItem, (pos) -> helper.pickup(entityItem));
        } else if (entity.hasStuffInInventory()) {
            helper.putStuffAway();
        }
    }

}
