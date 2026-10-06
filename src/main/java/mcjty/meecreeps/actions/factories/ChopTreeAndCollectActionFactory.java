package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.ChopTreeAndCollectActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ChopTreeAndCollectActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        BlockState state = world.getBlockState(pos);
        if (state.is(net.minecraft.tags.BlockTags.LOGS)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new ChopTreeAndCollectActionWorker(helper);
    }
}
