package mcjty.meecreeps.actions.workers;

import net.minecraft.core.Direction;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class LightupActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(options.getTargetPos().offset(-10, -5, -10), options.getTargetPos().offset(10, 5, 10));
        }
        return actionBox;
    }


    public LightupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private BlockPos findDarkSpot() {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        AABB box = getActionBox();
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (world.isEmptyBlock(p) && world.getBlockState(p.below()).isFaceSturdy(world, p.below(), Direction.UP)) {
                // Ignore daylight so exposed areas are also prepared for night.
                int light = world.getBrightness(LightLayer.BLOCK, p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        int light = world.getBrightness(LightLayer.BLOCK, pos);
        if (light < 7 && world.isEmptyBlock(pos)
                && world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
            ItemStack torch = entity.consumeItem(WorkerHelper::isTorch, 1);
            if (!torch.isEmpty()) {
                helper.placeStackAt(torch, world, pos);
                entity.addStack(torch);
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
