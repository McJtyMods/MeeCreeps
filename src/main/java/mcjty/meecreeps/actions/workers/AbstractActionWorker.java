package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IActionContext;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.api.PreferedChest;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;

public abstract class AbstractActionWorker implements IActionWorker {

    private static final PreferedChest[] PREFERED_CHESTS = new PreferedChest[]{
            PreferedChest.TARGET,
            PreferedChest.MARKED,
            PreferedChest.LAST_CHEST};

    private AxisAlignedBB searchBox = null;

    protected final IWorkerHelper helper;
    protected final IActionContext options;

    public AbstractActionWorker(IWorkerHelper helper) {
        this.helper = helper;
        this.options = helper.getContext();
    }

    @Nonnull
    @Override
    public AxisAlignedBB getSearchBox() {
        if (searchBox == null) {
            // @todo config
            searchBox = new AxisAlignedBB(options.getTargetPos().add(-12, -5, -12), options.getTargetPos().add(12, 5, 12));
        }
        return searchBox;
    }

    @Override
    public PreferedChest[] getPreferedChests() {
        return PREFERED_CHESTS;
    }

}
