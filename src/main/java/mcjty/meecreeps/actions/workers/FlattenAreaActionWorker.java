package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlattenAreaActionWorker extends AbstractActionWorker {

    private int size = 0;
    // We cannot break those so skip them
    private Set<BlockPos> positionsToSkip = new HashSet();


    public FlattenAreaActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public void init(IMeeCreep meeCreep) {
        helper.setSpeed(3);
    }

    @Nullable
    @Override
    public AxisAlignedBB getActionBox() {
        return null;
    }

    private int getSize() {
        if (size == 0) {
            String id = options.getFurtherQuestionId();
            if ("9x9".equals(id)) {
                size = 9;
            } else if ("11x11".equals(id)) {
                size = 11;
            } else {
                size = 13;
            }
        }
        return size;
    }

    /**
     * Returns absolute position
     */
    private BlockPos findSpotToFlatten() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos tpos = options.getTargetPos();
        int hs = (getSize() - 1) / 2;

        List<BlockPos> todo = new ArrayList<>();
        for (int x = -hs; x <= hs; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = -hs; z <= hs; z++) {
                    BlockPos relativePos = new BlockPos(x, y, z);
                    BlockPos p = tpos.add(relativePos);
                    if (!entity.getWorld().isAirBlock(p) && !positionsToSkip.contains(p)) {
                        todo.add(p);
                    }
                }
            }
        }
        if (todo.isEmpty()) {
            return null;
        }

        BlockPos position = entity.getEntity().getPosition();
        todo.sort((o1, o2) -> {
            double d1 = position.distanceSq(o1);
            double d2 = position.distanceSq(o2);
            return Double.compare(d1, d2);
        });
        return todo.get(0);
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
            return;
        }
        handleFlatten();
    }

    private void handleFlatten() {
        BlockPos flatSpot = findSpotToFlatten();
        if (flatSpot == null) {
            helper.taskIsDone();
        } else {
            BlockPos navigate = helper.findBestNavigationSpot(flatSpot);
            if (navigate != null) {
                helper.navigateTo(navigate, p -> {
                    helper.delayForHardBlocks(flatSpot, pp -> {
                        if (!helper.harvestAndDrop(flatSpot)) {
                            positionsToSkip.add(flatSpot);
                        }
                    });
                });
            } else {
                // We couldn't reach it. Just drop the block
                helper.delayForHardBlocks(flatSpot, pp -> {
                    if (!helper.harvestAndDrop(flatSpot)) {
                        positionsToSkip.add(flatSpot);
                    }
                });
            }
        }
    }

}