package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.LightupActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import org.jspecify.annotations.NonNull;

public class LightupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        // @todo config for area
        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(-10, -5, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(10, 5, 10)));
//        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(-2, -2, -2)), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(2, 2, 2)));
        return GeneralTools.traverseBoxTest(box, p -> {
            if (world.isEmptyBlock(p) && world.getBlockState(p.below()).isFaceSturdy(world, p.below(), Direction.UP)) {
                // Ignore daylight so exposed areas are also prepared for night.
                int light = world.getBrightness(LightLayer.BLOCK, p);
                if (light < 7) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new LightupActionWorker(helper);
    }
}
