package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.BuildProgress;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBuildActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;
    private BuildProgress progress = new BuildProgress(2, 0);
    // Set of relative positions to skip because they need optional materials
    private Set<BlockPos> toSkip = new HashSet<>();

    protected IBuildSchematic schematic = null;

    public AbstractBuildActionWorker(IWorkerHelper helper) {
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
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(options.getTargetPos().offset(-12, -5, -12), options.getTargetPos().offset(12, 5, 12));
        }
        return actionBox;
    }

    protected abstract IBuildSchematic getSchematic();

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
            return;
        }
        if (progress.getHeight() == 0) {
            if (!helper.handleFlatten(getSchematic())) {
                // Continue with building
                progress.setHeight(1);
                progress.setPass(0);
                helper.setSpeed(5);
            }
        } else {
            if (!helper.handleBuilding(getSchematic(), progress, toSkip)) {
                helper.taskIsDone();
            }
        }
    }


    @Override
    public void readFromNBT(CompoundTag tag) {
        progress.setHeight(tag.getInt("stage"));
        progress.setPass(tag.getInt("pass"));
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("stage", progress.getHeight());
        tag.putInt("pass", progress.getPass());
    }
}