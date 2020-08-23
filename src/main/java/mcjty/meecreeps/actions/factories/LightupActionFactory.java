package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.LightupActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.WorldEntitySpawner;

import javax.annotation.Nonnull;

public class LightupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        // @todo config for area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));
//        AxisAlignedBB box = new AxisAlignedBB(pos.add(-2, -2, -2), pos.add(2, 2, 2));
        // todo: confirm this works
        return GeneralTools.traverseBoxTest(box, p -> {
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, world, p, EntityType.ZOMBIE)) {
                int light = world.getLight(p);
                if (light < 7) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new LightupActionWorker(helper);
    }
}
