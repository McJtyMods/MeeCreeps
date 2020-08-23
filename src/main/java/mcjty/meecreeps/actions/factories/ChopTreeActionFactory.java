package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.ChopTreeActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.LogBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ChopTreeActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof LogBlock) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new ChopTreeActionWorker(helper);
    }
}
