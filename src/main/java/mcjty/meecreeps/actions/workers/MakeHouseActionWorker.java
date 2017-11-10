package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.schematics.SchematicHouse;
import mcjty.meecreeps.api.BuildProgress;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class MakeHouseActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;
    private BuildProgress progress = new BuildProgress(2, 0);
    // Set of relative positions to skip because they need optional materials
    private Set<BlockPos> toSkip = new HashSet<>();

    private IBuildSchematic schematic = null;

    public MakeHouseActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public void init() {
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

    private IBuildSchematic getSchematic() {
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
            schematic = new SchematicHouse(size, helper);
        }
        return schematic;
    }

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