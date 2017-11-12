package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.schematics.SchematicHouse;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IWorkerHelper;

public class MakeHouseActionWorker extends AbstractBuildActionWorker {

    public MakeHouseActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
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
            schematic = new SchematicHouse(size, helper);
        }
        return schematic;
    }


}