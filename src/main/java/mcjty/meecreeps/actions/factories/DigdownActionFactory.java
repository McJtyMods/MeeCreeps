package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.DigdownActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

public class DigdownActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        if (side != Direction.UP) {
            return false;
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null) {
            return false;
        }
//        Block block = world.getBlockState(pos).getBlock();
//        if (block.is)
        return true;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new DigdownActionWorker(helper);
    }
}
