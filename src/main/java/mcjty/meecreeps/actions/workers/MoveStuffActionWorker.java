package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MoveStuffActionWorker extends AbstractActionWorker {

    public MoveStuffActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
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

        if (timeToWrapUp) {
            meeCreep.placeDownBlock(meeCreep.blockPosition());
            helper.done();
        } else {
            Player player = options.getPlayer();

            if (meeCreep.getHeldBlockState() == null && player != null) {
                pickupBlock();
            }

            if (player == null) {
                // No player, time to stop.
                helper.taskIsDone();
            } else if (player.level().dimension() != meeCreep.level().dimension()) {
                // Wrong dimension, do nothing as this is handled by ServerActionManager
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {
                });
            }
        }
    }

    private void pickupBlock() {
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) helper.getMeeCreep();

        Level world = meeCreep.getWorld();
        BlockPos pos = options.getTargetPos();
        BlockState state = world.getBlockState(pos);
        if (!helper.allowedToHarvest(state, world, pos, options.getPlayer())) {
            helper.showMessage("message.meecreeps.cant_pickup_block");
            helper.taskIsDone();
            return;
        }
        meeCreep.setHeldBlockState(state);

        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity != null) {
            CompoundTag tc = new CompoundTag();
            tc = tileEntity.saveWithFullMetadata();
            world.removeBlockEntity(pos);
            tc.remove("x");
            tc.remove("y");
            tc.remove("z");
            meeCreep.setCarriedNBT(tc);
        }
        world.removeBlock(pos, false);
    }
}
