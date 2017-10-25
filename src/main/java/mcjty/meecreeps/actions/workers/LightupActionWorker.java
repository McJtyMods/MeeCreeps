package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class LightupActionWorker extends AbstractActionWorker {

    public LightupActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        super(entity, options);
    }

    private BlockPos findDarkSpot() {
        World world = entity.getEntityWorld();
        BlockPos pos = options.getPos();
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, p)) {
                int light = world.getLightFromNeighbors(p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        entity.getEntityWorld().setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
    }

    @Override
    protected void performTick(boolean lastTask) {
        if (lastTask) {
            done();
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                navigateTo(darkSpot, () -> placeTorch(darkSpot));
            }
        }
    }

}