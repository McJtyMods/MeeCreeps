package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
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

public class PickupActionWorker implements IActionWorker {

    private final ActionOptions options;

    private AxisAlignedBB actionBox = null;
    private EntityItem movingToItem = null;
    private boolean needsToPutAway = false;
    private boolean movingToChest = false;
    private int waitABit = 10;

    public PickupActionWorker(ActionOptions options) {
        this.options = options;
    }

    private AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -10, -10), options.getPos().add(10, 10, 10));
        }
        return actionBox;
    }


    private void pickup(EntityMeeCreeps entity, EntityItem item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }


    @Override
    public void tick(EntityMeeCreeps entity, boolean lastTask) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = 10;

        BlockPos position = entity.getPosition();

        if (needsToPutAway) {
            findChestToPutItemsIn(entity, position);
        } else if (movingToChest) {
            movingToChest = false; // Automatically return to the first state
        } else if (movingToItem != null && !movingToItem.isDead) {
            tryToMoveToItem(entity, position);
        } else if (lastTask) {
            options.setStage(Stage.DONE);
            ServerActionManager.getManager().save();
        } else {
            tryFindingItemsToPickup(entity, position);
        }
    }

    private void tryFindingItemsToPickup(EntityMeeCreeps entity, BlockPos position) {
        movingToItem = null;
        List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, getActionBox());
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            double d = position.distanceSq(entityItem.posX, entityItem.posY, entityItem.posZ);
            if (d < 2) {
                pickup(entity, entityItem);
            } else {
                if (!entity.getNavigator().tryMoveToEntityLiving(entityItem, 2.0)) {
                    // We need to teleport
                    entity.setPositionAndUpdate(entityItem.posX, entityItem.posY, entityItem.posZ);
                }
                movingToItem = entityItem;
            }
        } else if (!entity.getInventory().isEmpty()) {
            needsToPutAway = true;
        }
    }

    private void tryToMoveToItem(EntityMeeCreeps entity, BlockPos position) {
        double d = position.distanceSq(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
        if (d < 2) {
            pickup(entity, movingToItem);
            movingToItem = null;
        } else if (entity.getNavigator().noPath()) {
            if (!entity.getNavigator().tryMoveToEntityLiving(movingToItem, 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
            }
        }
    }

    private void findChestToPutItemsIn(EntityMeeCreeps entity, BlockPos position) {
        BlockPos pos = options.getPos();
        double d = position.distanceSq(pos);
        if (d < 2) {
            List<ItemStack> remainingItems = new ArrayList<>();
            TileEntity te = entity.getEntityWorld().getTileEntity(pos);
            if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                for (ItemStack stack : entity.getInventory()) {
                    ItemStack remaining = ItemHandlerHelper.insertItem(capability, stack, false);
                    if (!remaining.isEmpty()) {
                        remainingItems.add(remaining);
                    }
                }
                entity.getInventory().clear();
                for (ItemStack item : remainingItems) {
                    entity.addStack(item);
                }
            }
            if (!remainingItems.isEmpty()) {
                // Can't do anything
                options.setStage(Stage.DONE);
                ServerActionManager.getManager().save();
            }
            needsToPutAway = false;
        } else {
            if (!entity.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
            } else {
                movingToChest = true;
            }
        }
    }
}
