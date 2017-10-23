package mcjty.meecreeps.entities;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import net.minecraft.entity.ai.EntityAIBase;
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

public class MeeCreepWorkerTask extends EntityAIBase {

    private final EntityMeeCreeps meeCreeps;

    private AxisAlignedBB actionBox = null;
    private EntityItem movingToItem = null;
    private boolean needsToPutAway = false;
    private boolean movingToChest = false;
    private List<ItemStack> pickedUpItems = new ArrayList<>();
    private int waitABit = 10;

    public MeeCreepWorkerTask(EntityMeeCreeps meeCreeps) {
        this.meeCreeps = meeCreeps;
    }

    @Override
    public boolean shouldExecute() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.getStage() == Stage.WORKING) {
                    return true;
                }
            }
        }
        return false;
    }

    private ActionOptions getOptions() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            return options;
        }
        throw new RuntimeException("This cannot happen!");
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
    }

    private AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            ActionOptions options = getOptions();
            if (options == null) {
                throw new RuntimeException("Something went wrong!");
            }
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -10, -10), options.getPos().add(10, 10, 10));
        }
        return actionBox;
    }

    private void pickup(EntityItem item) {
        pickedUpItems.add(item.getItem().copy());
        if (pickedUpItems.size() >= 3) {    // @todo configurable amount
            needsToPutAway = true;
        }
        item.setDead();
    }

    private void pickupItem(ActionOptions options) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        waitABit = 10;

        BlockPos position = meeCreeps.getPosition();

        if (needsToPutAway) {
            BlockPos pos = options.getPos();
            double d = position.distanceSq(pos);
            if (d < 2) {
                boolean couldNotPutAway = false;
                TileEntity te = meeCreeps.getEntityWorld().getTileEntity(pos);
                if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                    IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                    for (ItemStack stack : pickedUpItems) {
                        ItemStack remaining = ItemHandlerHelper.insertItem(capability, stack, false);
                        if (!remaining.isEmpty()) {
                            EntityItem item = new EntityItem(meeCreeps.getEntityWorld(), meeCreeps.posX, meeCreeps.posY+1, meeCreeps.posZ, remaining);
                            meeCreeps.getEntityWorld().spawnEntity(item);
                            couldNotPutAway = true;
                        }
                    }
                    pickedUpItems.clear();
                } else {
                    couldNotPutAway = true;
                }
                if (couldNotPutAway) {
                    // Can't do anything
                    options.setStage(Stage.DONE);
                    ServerActionManager.getManager().save();
                }
                needsToPutAway = false;
            } else {
                if (!meeCreeps.getNavigator().tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), 2.0)) {
                    // We need to teleport
                    meeCreeps.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                } else {
                    movingToChest = true;
                }
            }
        } else if (movingToChest) {
            movingToChest = false; // Automatically return to the first state
        } else if (movingToItem != null && !movingToItem.isDead) {
            double d = position.distanceSq(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
            if (d < 2) {
                pickup(movingToItem);
                movingToItem = null;
            } else if (meeCreeps.getNavigator().noPath()) {
                if (!meeCreeps.getNavigator().tryMoveToEntityLiving(movingToItem, 2.0)) {
                    // We need to teleport
                    meeCreeps.setPositionAndUpdate(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
                }
            }
        } else {
            List<EntityItem> items = meeCreeps.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, getActionBox());
            // @todo find closest item
            if (!items.isEmpty()) {
                items.sort((o1, o2) -> {
                    double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                    double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                    return Double.compare(d1, d2);
                });
                EntityItem entityItem = items.get(0);
                double d = position.distanceSq(entityItem.posX, entityItem.posY, entityItem.posZ);
                if (d < 2) {
                    pickup(entityItem);
                } else {
                    if (!meeCreeps.getNavigator().tryMoveToEntityLiving(entityItem, 2.0)) {
                        // We need to teleport
                        meeCreeps.setPositionAndUpdate(entityItem.posX, entityItem.posY, entityItem.posZ);
                    }
                    movingToItem = entityItem;
                }
            } else if (!pickedUpItems.isEmpty()) {
                needsToPutAway = true;
            }
        }
    }

    @Override
    public void updateTask() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.getStage() == Stage.WORKING) {
                    switch (options.getTask()) {
                        case ACTION_HARVEST:
                            break;
                        case ACTION_PLACE_TORCHES:
                            break;
                        case ACTION_PICKUP_ITEMS:
                            pickupItem(options);
                            break;
                    }
                }
            }
        }
    }
}
