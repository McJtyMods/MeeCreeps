package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.MoveStuffActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class MoveStuffActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        if (world.getTileEntity(pos) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new MoveStuffActionWorker(helper);
    }
}
