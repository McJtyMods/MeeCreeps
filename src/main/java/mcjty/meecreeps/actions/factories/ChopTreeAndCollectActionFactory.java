package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.ChopTreeAndCollectActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChopTreeAndCollectActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new ChopTreeAndCollectActionWorker(helper);
    }
}
