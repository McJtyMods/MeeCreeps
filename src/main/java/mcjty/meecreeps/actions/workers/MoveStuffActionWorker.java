package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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
    public void tick(boolean timeToWrapUp) {
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) this.entity;

        if (timeToWrapUp) {
            meeCreep.placeDownBlock(meeCreep.getPosition());
            helper.done();
        } else {
            EntityPlayer player = options.getPlayer();
            if (player == null) {
                // No player, time to stop.
                helper.taskIsDone();
            } else if (player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension()) {
                // Wrong dimension. Teleport to the player
                BlockPos p = helper.findSuitablePositionNearPlayer(3.0);
                TeleportationTools.teleportEntity(meeCreep, player.getEntityWorld(), p.getX(), p.getY(), p.getZ(), EnumFacing.NORTH);
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(3.0);
                helper.navigateTo(p, blockPos -> {});
            }
        }
    }

    @Override
    public void init() {
        pickupBlock();
    }

    private void pickupBlock() {
        World world = entity.getWorld();
        BlockPos pos = options.getTargetPos();
        IBlockState state = world.getBlockState(pos);
        if (!helper.allowedToHarvest(state, world, pos, options.getPlayer())) {
            helper.showMessage("I cannot pick up this block!");
            helper.taskIsDone();
            return;
        }

        EntityMeeCreeps meeCreep = (EntityMeeCreeps) this.entity;
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
