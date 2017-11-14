package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.util.math.AxisAlignedBB;

public class AngryActionWorker extends AbstractActionWorker {

    @Override
    public AxisAlignedBB getActionBox() {
        return null;
    }

    @Override
    public void init() {
        ((EntityMeeCreeps) entity).setVariationFace(1);
    }

    public AngryActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
        }
    }
}