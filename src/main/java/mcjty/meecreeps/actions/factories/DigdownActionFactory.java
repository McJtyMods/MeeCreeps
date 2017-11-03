package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.DigdownActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DigdownActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos) {
        return false;
    }

    @Override
    public IActionWorker createWorker(IWorkerHelper helper) {
        return new DigdownActionWorker(helper);
    }
}
