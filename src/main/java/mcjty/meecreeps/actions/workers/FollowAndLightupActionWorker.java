package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class FollowAndLightupActionWorker extends AbstractActionWorker {

    public FollowAndLightupActionWorker(IWorkerHelper helper) {
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

    private BlockPos findDarkSpot() {
        World world = helper.getMeeCreep().getWorld();
        BlockPos position = options.getPlayer().getPosition();
        AxisAlignedBB box = new AxisAlignedBB(position.add(-6, -4, -6), position.add(6, 4, 6));
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (world.isAirBlock(p) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, p)) {
                int light = world.getLightFromNeighbors(p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        int light = world.getLightFromNeighbors(pos);
        if (light < 7) {
            ItemStack torch = entity.consumeItem(WorkerHelper::isTorch, 1);
            if (!torch.isEmpty()) {
                helper.placeStackAt(torch, world, pos);
            }
        }
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) entity;
        EntityPlayer player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (!entity.hasItem(WorkerHelper::isTorch)) {
            if (!helper.findItemOnGroundOrInChest(WorkerHelper::isTorch, Integer.MAX_VALUE, "message.meecreeps.cant_find_torches")) {
                helper.taskIsDone();
            }
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                helper.navigateTo(darkSpot, this::placeTorch);
            } else if (player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension()) {
                // Wrong dimension, do nothing as this is handled by ServerActionManager
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {});
            }
        }
    }
}
