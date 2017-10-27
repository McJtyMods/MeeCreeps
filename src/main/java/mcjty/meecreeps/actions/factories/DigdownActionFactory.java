package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionFactory;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.workers.DigdownActionWorker;
import mcjty.meecreeps.actions.workers.LightupActionWorker;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class DigdownActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos) {
        return false;
    }

    @Override
    public IActionWorker createWorker(EntityMeeCreeps entity, ActionOptions options) {
        return new DigdownActionWorker(entity, options);
    }
}
