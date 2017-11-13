package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.math.AxisAlignedBB;

public class IdleActionWorker extends AbstractActionWorker {

    @Override
    public AxisAlignedBB getActionBox() {
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