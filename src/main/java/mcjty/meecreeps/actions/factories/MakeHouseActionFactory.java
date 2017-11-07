package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.MakeHouseActionWorker;
import mcjty.meecreeps.actions.workers.MineOresActionWorker;
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

public class MakeHouseActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Nullable
    @Override
    public String getFurtherQuestionHeading(World world, BlockPos pos, EnumFacing side) {
        return "How big do you want the house?";
    }

    @Nonnull
    @Override
    public List<Pair<String, String>> getFurtherQuestions(World world, BlockPos pos, EnumFacing side) {
        List<Pair<String, String>> result = new ArrayList<>();
        result.add(Pair.of("9x9", "9x9 of course!"));
        result.add(Pair.of("11x11", "11x11 is cleary inferior but..."));
        result.add(Pair.of("13x13", "I need the room so take 13x13"));
        return result;
    }

    @Nullable
    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new MakeHouseActionWorker(helper);
    }
}
