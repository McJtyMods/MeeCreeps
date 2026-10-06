package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IActionContext;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.api.PreferedChest;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public abstract class AbstractActionWorker implements IActionWorker {

    private static final PreferedChest[] PREFERED_CHESTS = new PreferedChest[]{
            PreferedChest.TARGET,
            PreferedChest.MARKED,
            PreferedChest.LAST_CHEST};

    private AABB searchBox = null;

    protected final IWorkerHelper helper;
    protected final IActionContext options;

    public AbstractActionWorker(IWorkerHelper helper) {
        this.helper = helper;
        this.options = helper.getContext();
    }

    @Nonnull
    @Override
    public AABB getSearchBox() {
        if (searchBox == null) {
            // @todo config
            searchBox = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(-12, -5, -12)), net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(12, 5, 12)));
        }
        return searchBox;
    }

    @Override
    public PreferedChest[] getPreferedChests() {
        return PREFERED_CHESTS;
    }

}
