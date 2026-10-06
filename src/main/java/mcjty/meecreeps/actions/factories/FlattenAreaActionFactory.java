package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.FlattenAreaActionWorker;
import mcjty.meecreeps.actions.workers.MakeHouseActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FlattenAreaActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Nullable
    @Override
    public String getFurtherQuestionHeading(Level world, BlockPos pos, Direction side) {
        return "message.meecreeps.action.flatten_area_size";
    }

    @NonNull
    @Override
    public List<Pair<String, String>> getFurtherQuestions(Level world, BlockPos pos, Direction side) {
        List<Pair<String, String>> result = new ArrayList<>();
        result.add(Pair.of("9x9", "message.meecreeps.action.flatten_9x9"));
        result.add(Pair.of("11x11", "message.meecreeps.action.flatten_11x11"));
        result.add(Pair.of("13x13", "message.meecreeps.action.flatten_13x13"));
        return result;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new FlattenAreaActionWorker(helper);
    }
}
