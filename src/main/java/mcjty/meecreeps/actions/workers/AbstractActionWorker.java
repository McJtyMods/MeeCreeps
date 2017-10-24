package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractActionWorker implements IActionWorker {

    protected final ActionOptions options;
    protected final EntityMeeCreeps entity;
    protected boolean needsToPutAway = false;
    private int waitABit = 10;

    protected BlockPos movingToPos;
    protected Entity movingToEntity;
    protected Runnable job;

    public AbstractActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        this.options = options;
        this.entity = entity;
    }

    protected void navigateTo(BlockPos pos, Runnable job) {
        BlockPos position = entity.getPosition();
        double d = position.distanceSq(pos.getX(), pos.getY(), pos.getZ());
        if (d < 2) {
            job.run();
        } else if (!entity.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 2.0)) {
            // We need to teleport
            entity.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
            job.run();
        } else {
            this.movingToPos = pos;
            this.movingToEntity = null;
            this.job = job;
        }
    }

    protected void navigateTo(Entity dest, Runnable job) {
        BlockPos position = entity.getPosition();
        double d = position.distanceSq(dest.posX, dest.posY, dest.posZ);
        if (d < 2) {
            job.run();
        } else if (!entity.getNavigator().tryMoveToEntityLiving(dest, 2.0)) {
            // We need to teleport
            entity.setPositionAndUpdate(dest.posX, dest.posY, dest.posZ);
            job.run();
        } else {
            this.movingToPos = null;
            this.movingToEntity = dest;
            this.job = job;
        }
    }

    @Override
    public void tick(boolean lastTask) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = 10;

        if (job != null) {
            BlockPos position = entity.getPosition();
            if (movingToEntity != null) {
                if (movingToEntity.isDead) {
                    job = null;
                } else {
                    double d = position.distanceSq(movingToEntity.posX, movingToEntity.posY, movingToEntity.posZ);
                    if (d < 2) {
                        job.run();
                        job = null;
                    } else if (entity.getNavigator().noPath()) {
                        // It failed somehow. Try again
                        entity.getNavigator().tryMoveToEntityLiving(movingToEntity, 2.0);
                    }
                }
            } else {
                double d = position.distanceSq(movingToPos.getX(), movingToPos.getY(), movingToPos.getZ());
                if (d < 2) {
                    job.run();
                    job = null;
                } else if (entity.getNavigator().noPath()) {
                    // It failed somehow. Try again
                    entity.getNavigator().tryMoveToXYZ(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5, 2.0);
                }
            }
        } else {
            performTick(lastTask);
        }
    }

    protected void pickup(EntityItem item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }

    protected abstract void performTick(boolean lastTask);

    protected void done() {
        options.setStage(Stage.DONE);
        ServerActionManager.getManager().save();
    }

    protected void stashItems() {
        BlockPos pos = options.getPos();
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
            done();
        }
        needsToPutAway = false;
    }

    protected boolean needToFindChest(boolean lastTask) {
        return needsToPutAway || (lastTask && !entity.isEmptyInventory());
    }
}
