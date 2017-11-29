package mcjty.meecreeps.entities;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IActionWorker;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;

public class MeeCreepWorkerTask extends EntityAIBase {

    private final EntityMeeCreeps meeCreep;
    private WorkerHelper helper = null;

    public MeeCreepWorkerTask(EntityMeeCreeps meeCreep) {
        this.meeCreep = meeCreep;
    }

    @Override
    public boolean shouldExecute() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreep.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.getStage() == Stage.WORKING || options.getStage() == Stage.TIME_IS_UP || options.getStage() == Stage.TASK_IS_DONE) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Cancel the current job
    public void cancelJob() {
        if (helper != null) {
            helper.cancelJob();
        }
    }

    private WorkerHelper getHelper(ActionOptions options) {
        if (helper == null) {
            helper = new WorkerHelper(options);
            MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(options.getTask());
            IActionWorker worker = factory.getFactory().createWorker(helper);
            helper.setWorker(worker);
            worker.init(meeCreep);
        }
        return helper;
    }

    @Override
    public void updateTask() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreep.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.isPaused()) {
                    if (!meeCreep.getNavigator().noPath()) {
                        meeCreep.getNavigator().clearPath();
                    }
                } else {
                    WorkerHelper helper = getHelper(options);
                    if (options.getStage() == Stage.WORKING) {
                        helper.tick(meeCreep, false);
                    } else if (options.getStage() == Stage.TIME_IS_UP) {
                        if (helper.getWorker().onlyStopWhenDone()) {
                            helper.tick(meeCreep, false);
                        } else {
                            helper.tick(meeCreep, true);
                        }
                    } else if (options.getStage() == Stage.TASK_IS_DONE) {
                        helper.tick(meeCreep, true);
                    }
                }
            }
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (!meeCreep.getWorld().isRemote) {
            ServerActionManager manager = ServerActionManager.getManager();
            int actionId = meeCreep.getActionId();
            if (actionId != 0) {
                ActionOptions options = manager.getOptions(actionId);
                if (options != null && options.getTask() != null) {
                    getHelper(options).readFromNBT(tag);
                }
            }
        }
    }


    public WorkerHelper getHelper() {
        return helper;
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (helper != null) {
            helper.writeToNBT(tag);
        }
    }
}
