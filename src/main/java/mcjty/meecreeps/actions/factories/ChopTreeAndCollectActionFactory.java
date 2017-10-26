package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionFactory;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.workers.ChopTreeActionWorker;
import mcjty.meecreeps.actions.workers.ChopTreeAndCollectActionWorker;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChopTreeAndCollectActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos) {
        return false;
    }

    @Override
    public IActionWorker createWorker(EntityMeeCreeps entity, ActionOptions options) {
        return new ChopTreeAndCollectActionWorker(entity, options);
    }
}
