package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.MoveStuffActionWorker;
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

public class MoveStuffActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        if (world.getBlockEntity(pos) == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new MoveStuffActionWorker(helper);
    }
}
