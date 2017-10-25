package mcjty.meecreeps.entities;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;

public class MeeCreepWorkerTask extends EntityAIBase {

    private final EntityMeeCreeps meeCreeps;
    private IActionWorker worker = null;

    public MeeCreepWorkerTask(EntityMeeCreeps meeCreeps) {
        this.meeCreeps = meeCreeps;
    }

    @Override
    public boolean shouldExecute() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.getStage() == Stage.WORKING || options.getStage() == Stage.TIME_IS_UP) {
                    return true;
                }
            }
        }
        return false;
    }

    private IActionWorker getWorker(ActionOptions options) {
        if (worker == null) {
            worker = options.getTask().getActionFactory().createWorker(meeCreeps, options);
        }
        return worker;
    }

    @Override
    public void updateTask() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.isPaused()) {
                    if (!meeCreeps.getNavigator().noPath()) {
                        meeCreeps.getNavigator().clearPath();
                    }
                } else if (options.getStage() == Stage.WORKING) {
                    getWorker(options).tick(false);
                } else if (options.getStage() == Stage.TIME_IS_UP) {
                    getWorker(options).tick(true);
                }
            }
        }
    }

    public void readFromNBT(NBTTagCompound tag) {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                getWorker(options).readFromNBT(tag);
            }
        }
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (worker != null) {
            worker.writeToNBT(tag);
        }
    }
}
