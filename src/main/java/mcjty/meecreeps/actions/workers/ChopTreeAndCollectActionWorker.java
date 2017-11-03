package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.ForgeEventHandlers;
import mcjty.meecreeps.api.IActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.Counter;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

public class ChopTreeAndCollectActionWorker extends ChopTreeActionWorker {

    private AxisAlignedBB actionBox = null;

    public ChopTreeAndCollectActionWorker(EntityMeeCreeps entity, IActionOptions options) {
        super(entity, options);
    }

    @Override
    protected AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }

    private void harvest(BlockPos pos) {
        World world = entity.getEntityWorld();
        BlockPlanks.EnumType woodType = getWoodType(world.getBlockState(pos));
        harvestAndPickup(pos);
        findLeaves(pos, world, woodType);
    }

    @Override
    protected void performTick(boolean timeToWrapUp) {
        if (blocks.isEmpty()) {
            findTree();
        }
        if (blocks.isEmpty() && leavesToTick.isEmpty()) {
            // There is nothing left to do
            done();
            return;
        }

        if (!leavesToTick.isEmpty()) {
            decayLeaves();
        }

        if (timeToWrapUp) {
            if (entity.hasStuffInInventory()) {
                // We need to find a suitable chest
                if (!findSuitableInventory(getActionBox(), entity.getInventoryMatcher(), this::putInventoryInChest)) {
                    if (!navigateTo(getPlayer(), (p) -> giveToPlayerOrDrop(), 12)) {
                        entity.dropInventory();
                    }
                }
            } else {
                done();
            }
        } else if (!blocks.isEmpty()) {
            harvest(blocks.remove(0));
            // @todo config
            waitABit = 5;   // Speed up things
        } else {
            taskIsDone();
        }
    }

    private void decayLeaves() {
        World world = entity.getEntityWorld();
        Counter<BlockPos> newmap = new Counter<>();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!world.isAirBlock(pos)) {
                IBlockState state = world.getBlockState(pos);
                ForgeEventHandlers.harvestableBlocksToCollect.put(pos, options.getActionId());
                state.getBlock().updateTick(world, pos, state, entity.getRNG());

                if (!world.isAirBlock(pos)) {
                    Integer counter = entry.getValue();
                    counter--;
                    if (counter > 0) {
                        newmap.put(pos, counter);
                    }
                }
            }
        }
        leavesToTick = newmap;
    }

    @Override
    protected boolean findChestToPutItemsIn() {
        return findSuitableInventory(getActionBox(), entity.getInventoryMatcher(), this::putInventoryInChest);
    }
}
