package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class PickupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    public PickupActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        super(entity, options);
    }

    private AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -10, -10), options.getPos().add(10, 10, 10));
        }
        return actionBox;
    }


    @Override
    protected void performTick(boolean lastTask) {
        if (needToFindChest(lastTask)) {
            findChestToPutItemsIn();
        } else if (lastTask) {
            done();
        } else {
            tryFindingItemsToPickup();
        }
    }

    private void tryFindingItemsToPickup() {
        BlockPos position = entity.getPosition();
        List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            navigateTo(entityItem, () -> pickup(entityItem));
        } else if (!entity.getInventory().isEmpty()) {
            needsToPutAway = true;
        }
    }

    private void findChestToPutItemsIn() {
        BlockPos pos = options.getPos();
        navigateTo(pos, this::stashItems);
    }

}
