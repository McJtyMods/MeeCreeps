package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.FlattenAreaActionWorker;
import mcjty.meecreeps.actions.workers.MakeHouseActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FlattenAreaActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Nullable
    @Override
    public String getFurtherQuestionHeading(World world, BlockPos pos, EnumFacing side) {
        return "What area do you want to flatten?";
    }

    @Nonnull
    @Override
    public List<Pair<String, String>> getFurtherQuestions(World world, BlockPos pos, EnumFacing side) {
        List<Pair<String, String>> result = new ArrayList<>();
        result.add(Pair.of("9x9", "9x9 is good enough"));
        result.add(Pair.of("11x11", "11x11 please"));
        result.add(Pair.of("13x13", "I'm going to need 13x13"));
        return result;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new FlattenAreaActionWorker(helper);
    }
}
