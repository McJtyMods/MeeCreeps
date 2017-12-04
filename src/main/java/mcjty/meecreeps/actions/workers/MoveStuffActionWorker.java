package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MoveStuffActionWorker extends AbstractActionWorker {

    public MoveStuffActionWorker(IWorkerHelper helper) {
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

        if (timeToWrapUp) {
            meeCreep.placeDownBlock(meeCreep.getPosition());
            helper.done();
        } else {
            EntityPlayer player = options.getPlayer();

            if (meeCreep.getHeldBlockState() == null && player != null) {
                pickupBlock();
            }

            if (player == null) {
                // No player, time to stop.
                helper.taskIsDone();
            } else if (player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension()) {
                // Wrong dimension, do nothing as this is handled by ServerActionManager
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {});
            }
        }
    }

    private void pickupBlock() {
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) helper.getMeeCreep();

        World world = meeCreep.getWorld();
        BlockPos pos = options.getTargetPos();
        IBlockState state = world.getBlockState(pos);
        if (!helper.allowedToHarvest(state, world, pos, options.getPlayer())) {
            helper.showMessage("message.meecreeps.cant_pickup_block");
            helper.taskIsDone();
            return;
        }
        meeCreep.setHeldBlockState(state);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity != null) {
            NBTTagCompound tc = new NBTTagCompound();
            tileEntity.writeToNBT(tc);
            world.removeTileEntity(pos);
            tc.removeTag("x");
            tc.removeTag("y");
            tc.removeTag("z");
            meeCreep.setCarriedNBT(tc);
        }
        world.setBlockToAir(pos);
    }
}
