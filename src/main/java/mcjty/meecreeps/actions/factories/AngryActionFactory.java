package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.AngryActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class AngryActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        // Always impossible. Can only be spawned by other MeeCreeps
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new AngryActionWorker(helper);
    }
}
