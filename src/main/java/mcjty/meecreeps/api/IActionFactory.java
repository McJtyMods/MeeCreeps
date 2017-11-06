package mcjty.meecreeps.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * A factory for actions
 */
public interface IActionFactory {

    /**
     * Return true if this action is possible given the targetted block and
     * surroundings
     */
    boolean isPossible(World world, BlockPos pos, EnumFacing side);

    /**
     * Return true if this action is possible given the targetted block
     * but maybe not with surroundings. i.e. it is possible to do this but
     * some items may be missing or some circumstances may be less ideal for this
     * task
     */
    boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side);

    /**
     * Actually create the action. If this is a 'question' factory then
     * this will return null
     */
    IActionWorker createWorker(@Nonnull IWorkerHelper helper);
}
