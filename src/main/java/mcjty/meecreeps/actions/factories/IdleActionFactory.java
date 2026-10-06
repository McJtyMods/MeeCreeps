package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.IdleActionWorker;
import mcjty.meecreeps.actions.workers.PickupActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

public class IdleActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return InventoryTools.isInventory(world, pos);
    }

    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new IdleActionWorker(helper);
    }
}
