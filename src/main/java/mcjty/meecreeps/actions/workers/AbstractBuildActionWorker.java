package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.BuildProgress;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBuildActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;
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
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-12, -5, -12), options.getTargetPos().add(12, 5, 12));
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
    public void readFromNBT(CompoundNBT tag) {
        progress.setHeight(tag.getInt("stage"));
        progress.setPass(tag.getInt("pass"));
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putInt("stage", progress.getHeight());
        tag.putInt("pass", progress.getPass());
    }
}