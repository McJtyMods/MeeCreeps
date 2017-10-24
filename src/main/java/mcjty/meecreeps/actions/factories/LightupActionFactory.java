package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.IActionFactory;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LightupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos) {
        return false;
    }

    @Override
    public IActionWorker createWorker(EntityMeeCreeps entity, ActionOptions options) {
        return null;
    }
}
