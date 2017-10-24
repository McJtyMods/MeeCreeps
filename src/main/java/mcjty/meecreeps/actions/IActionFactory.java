package mcjty.meecreeps.actions;

import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IActionFactory {

    /**
     * Return true if this action is possible given the targetted block and
     * surroundings
     */
    boolean isPossible(World world, BlockPos pos);

    /**
     * Return true if this action is possible given the targetted block
     * but maybe not with surroundings
     */
    boolean isPossibleSecondary(World world, BlockPos pos);

    IActionWorker createWorker(EntityMeeCreeps entity, ActionOptions options);
}
