package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.PickupActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PickupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for pickup area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -10, -10), pos.add(10, 10, 10));
        return !world.getEntitiesWithinAABB(ItemEntity.class, box).isEmpty();
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        return InventoryTools.isInventory(world, pos);
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new PickupActionWorker(helper);
    }
}
