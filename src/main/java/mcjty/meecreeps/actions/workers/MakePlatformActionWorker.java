package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.schematics.SchematicPlatform;
import mcjty.meecreeps.api.BuildProgress;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class MakePlatformActionWorker extends AbstractActionWorker {

    protected IBuildSchematic schematic = null;
    private AxisAlignedBB actionBox = null;
    private BuildProgress progress = new BuildProgress(1, 0);
    // Set of relative positions to skip because they need optional materials
    private Set<BlockPos> toSkip = new HashSet<>();

    public MakePlatformActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    protected IBuildSchematic getSchematic() {
        if (schematic == null) {
            int size;
            String id = options.getFurtherQuestionId();
            if ("9x9".equals(id)) {
                size = 9;
            } else if ("11x11".equals(id)) {
                size = 11;
            } else {
                size = 13;
            }
            schematic = new SchematicPlatform(size, helper);
        }
        return schematic;
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public void init(IMeeCreep meeCreep) {
        helper.setSpeed(5);
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

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
            return;
        }
        if (!helper.handleBuilding(getSchematic(), progress, toSkip)) {
            helper.taskIsDone();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        progress.setHeight(tag.getInteger("stage"));
        progress.setPass(tag.getInteger("pass"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("stage", progress.getHeight());
        tag.setInteger("pass", progress.getPass());
    }
}