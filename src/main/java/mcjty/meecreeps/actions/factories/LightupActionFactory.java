package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.LightupActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

import javax.annotation.Nonnull;

public class LightupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        // @todo config for area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));
//        AxisAlignedBB box = new AxisAlignedBB(pos.add(-2, -2, -2), pos.add(2, 2, 2));
        return GeneralTools.traverseBoxTest(box, p -> {
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, p)) {
                int light = world.getLightFromNeighbors(p);
                if (light < 7) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new LightupActionWorker(helper);
    }
}
