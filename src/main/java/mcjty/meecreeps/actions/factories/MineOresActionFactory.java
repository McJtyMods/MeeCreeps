package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.MineOresActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MineOresActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        return false;
        // @todo
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new MineOresActionWorker(helper);
    }
}
