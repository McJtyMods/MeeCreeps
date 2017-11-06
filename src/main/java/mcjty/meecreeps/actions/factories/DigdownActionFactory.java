package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.DigdownActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DigdownActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        if (side != EnumFacing.UP) {
            return false;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            return false;
        }
//        Block block = world.getBlockState(pos).getBlock();
//        if (block.is)
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new DigdownActionWorker(helper);
    }
}
