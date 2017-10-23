package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.IActionFactory;
import mcjty.meecreeps.actions.workers.PickupActionWorker;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PickupActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for pickup area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -10, -10), pos.add(10, 10, 10));
        return !world.getEntitiesWithinAABB(EntityItem.class, box).isEmpty();
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos) {
        return InventoryTools.isInventory(world, pos);
    }

    @Override
    public IActionWorker createWorker(ActionOptions options) {
        return new PickupActionWorker(options);
    }
}
