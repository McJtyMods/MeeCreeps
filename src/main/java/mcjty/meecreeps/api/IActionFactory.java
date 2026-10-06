package mcjty.meecreeps.api;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
    boolean isPossible(Level world, BlockPos pos, Direction side);

    /**
     * Return true if this action is possible given the targetted block
     * but maybe not with surroundings. i.e. it is possible to do this but
     * some items may be missing or some circumstances may be less ideal for this
     * task
     */
    boolean isPossibleSecondary(Level world, BlockPos pos, Direction side);

    /**
     * Optionally return a heading for further questions. If this returns null then
     * there are no further questions. This is called client-side!
     */
    @Nullable
    default String getFurtherQuestionHeading(Level world, BlockPos pos, Direction side) {
        return null;
    }

    /**
     * Return a list of possible further questions. If there are no further questions this will
     * return an empty list. The array should be a pair of Id and question. The question will be
     * asked to the user and the id is what will be given to the action when it is finally executed
     * This is called client-side!
     */
    @NonNull
    default List<Pair<String, String>> getFurtherQuestions(Level world, BlockPos pos, Direction side) {
        return Collections.emptyList();
    }

    /**
     * Actually create the action. If this is a 'question' factory then
     * this will return null
     */
    IActionWorker createWorker(@NonNull IWorkerHelper helper);
}
