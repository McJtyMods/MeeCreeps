package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.*;

public abstract class AbstractActionWorker implements IActionWorker {

    private static final PreferedChest[] PREFERED_CHESTS = new PreferedChest[]{
            PreferedChest.TARGET,
            PreferedChest.LAST_CHEST};

    protected final IWorkerHelper helper;
    protected final IMeeCreep entity;
    protected final IActionContext options;

    public AbstractActionWorker(IWorkerHelper helper) {
        this.helper = helper;
        this.entity = helper.getMeeCreep();
        this.options = helper.getContext();
    }

    @Override
    public PreferedChest[] getPreferedChests() {
        return PREFERED_CHESTS;
    }
}
