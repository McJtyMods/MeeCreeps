package mcjty.meecreeps.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
     * Optionally return a heading for further questions. If this returns null then
     * there are no further questions. This is called client-side!
     */
    @Nullable
    default String getFurtherQuestionHeading(World world, BlockPos pos, EnumFacing side) { return null; }

    /**
     * Return a list of possible further questions. If there are no further questions this will
     * return an empty list. The array should be a pair of Id and question. The question will be
     * asked to the user and the id is what will be given to the action when it is finally executed
     * This is called client-side!
     */
    @Nonnull
    default List<Pair<String, String>> getFurtherQuestions(World world, BlockPos pos, EnumFacing side) { return Collections.emptyList(); }

    /**
     * Actually create the action. If this is a 'question' factory then
     * this will return null
     */
    IActionWorker createWorker(@Nonnull IWorkerHelper helper);
}
