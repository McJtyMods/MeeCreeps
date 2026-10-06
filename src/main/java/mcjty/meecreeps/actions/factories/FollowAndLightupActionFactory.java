package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.FollowAndLightupActionWorker;
import mcjty.meecreeps.actions.workers.MoveStuffActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

public class FollowAndLightupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new FollowAndLightupActionWorker(helper);
    }
}
