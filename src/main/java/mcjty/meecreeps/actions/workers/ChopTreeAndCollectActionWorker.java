package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.Counter;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.api.PreferedChest;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public class ChopTreeAndCollectActionWorker extends ChopTreeActionWorker {

    private static final PreferedChest[] PREFERED_CHESTS = new PreferedChest[]{
            PreferedChest.MARKED,
            PreferedChest.FIND_MATCHING_INVENTORY};

    private AABB actionBox = null;

    public ChopTreeAndCollectActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(options.getTargetPos().offset(-10, -5, -10), options.getTargetPos().offset(10, 5, 10));
        }
        return actionBox;
    }

    private void harvest(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        helper.harvestAndPickup(pos);
        findLeaves(pos, world);
    }

    @Override
    public void tick(boolean timeToWrapUp) {

        if (timeToWrapUp) {
            helper.done();
            return;
        }

        if (blocks.isEmpty()) {
            findTree();
        }
        if (blocks.isEmpty() && leavesToTick.isEmpty()) {
            // There is nothing left to do
            helper.taskIsDone();
            return;
        }

        if (!leavesToTick.isEmpty()) {
            decayLeaves();
        }

        if (!blocks.isEmpty()) {
            BlockPos toRemove = blocks.remove(0);
            helper.navigateTo(options.getTargetPos(), blockPos -> harvest(toRemove));
        } else {
            helper.taskIsDone();
        }
    }

    private void decayLeaves() {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        Counter<BlockPos> newmap = new Counter<>();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!world.isEmptyBlock(pos)) {
                BlockState state = world.getBlockState(pos);
                helper.registerHarvestableBlock(pos);
                state.randomTick((net.minecraft.server.level.ServerLevel) world, pos, entity.getRandom());

                if (!world.isEmptyBlock(pos)) {
                    Integer counter = entry.getValue();
                    counter--;
                    if (counter > 0) {
                        newmap.put(pos, counter);
                    }
                }
            }
        }
        leavesToTick = newmap;
    }

    @Override
    public PreferedChest[] getPreferedChests() {
        return PREFERED_CHESTS;
    }
}
