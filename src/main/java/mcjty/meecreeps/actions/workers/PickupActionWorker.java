package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PickupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    public PickupActionWorker(EntityMeeCreeps entity, IActionOptions options) {
        super(entity, options);
    }

    @Override
    protected AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -10, -10), options.getTargetPos().add(10, 10, 10));
        }
        return actionBox;
    }


    @Override
    protected void performTick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            done();
        } else {
            tryFindingItemsToPickup();
        }
    }

    @Override
    protected void tryFindingItemsToPickup() {
        BlockPos position = entity.getPosition();
        List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            navigateTo(entityItem, (pos) -> pickup(entityItem));
        } else if (!entity.getInventory().isEmpty()) {
            needsToPutAway = true;
        }
    }

}
