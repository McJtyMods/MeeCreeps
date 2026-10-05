package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.world.phys.AABB;

public class IdleActionWorker extends AbstractActionWorker {

    @Override
    public AABB getActionBox() {
        return null;
    }


    public IdleActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
        }
    }
}