package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.IdleActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class IdleActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return InventoryTools.isInventory(world, pos);
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new IdleActionWorker(helper);
    }
}
