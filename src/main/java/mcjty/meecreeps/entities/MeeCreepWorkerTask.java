package mcjty.meecreeps.entities;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import net.minecraft.entity.ai.EntityAIBase;

public class MeeCreepWorkerTask extends EntityAIBase {

    private final EntityMeeCreeps meeCreeps;

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
                if (options.getStage() == Stage.WORKING) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
    }

    @Override
    public void updateTask() {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = meeCreeps.getActionId();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options != null) {
                if (options.getStage() == Stage.WORKING) {
                    switch (options.getTask()) {
                        case ACTION_HARVEST:
                            break;
                        case ACTION_PLACE_TORCHES:
                            break;
                        case ACTION_PICKUP_ITEMS:
                            System.out.println("MeeCreepWorkerTask.updateTask: PICKUP");
                            break;
                    }
                }
            }
        }
    }
}
