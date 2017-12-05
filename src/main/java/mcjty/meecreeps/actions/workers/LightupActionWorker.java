package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class LightupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }


    public LightupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private BlockPos findDarkSpot() {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        AxisAlignedBB box = getActionBox();
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
        if (timeToWrapUp) {
            helper.done();
        } else if (!entity.hasItem(WorkerHelper::isTorch)) {
            helper.findItemOnGroundOrInChest(WorkerHelper::isTorch, 128, "message.meecreeps.cant_find_torches");
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                helper.navigateTo(darkSpot, this::placeTorch);
            } else {
                helper.taskIsDone();
            }
        }
    }

}