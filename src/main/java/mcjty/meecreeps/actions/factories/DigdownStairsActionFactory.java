package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.DigdownStairsActionWorker;
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

public class DigdownStairsActionFactory implements IActionFactory {

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
        return "message.meecreeps.action.what_direction";
    }

    @Nonnull
    @Override
    public List<Pair<String, String>> getFurtherQuestions(World world, BlockPos pos, EnumFacing side) {
        List<Pair<String, String>> result = new ArrayList<>();
        result.add(Pair.of(EnumFacing.NORTH.getName(), "message.meecreeps.action.to_north"));
        result.add(Pair.of(EnumFacing.SOUTH.getName(), "message.meecreeps.action.to_south"));
        result.add(Pair.of(EnumFacing.WEST.getName(), "message.meecreeps.action.to_west"));
        result.add(Pair.of(EnumFacing.EAST.getName(), "message.meecreeps.action.to_east"));
        return result;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new DigdownStairsActionWorker(helper);
    }
}
