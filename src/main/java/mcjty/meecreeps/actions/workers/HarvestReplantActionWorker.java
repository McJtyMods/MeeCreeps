package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.BlockItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestReplantActionWorker extends HarvestActionWorker {

    private Map<BlockPos, Block> needToReplant = new HashMap<>();

    public HarvestReplantActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private void replant(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        Block block = needToReplant.get(pos);
        needToReplant.remove(pos);
        for (ItemStack stack : entity.getInventory()) {
            if (stack.getItem() instanceof BlockItem) {
                BlockState plant = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                if (plant.getBlock() == block) {
                    // This is a valid seed
                    stack.split(1);
                    world.setBlock(pos, plant, 3);
                    break;
                }
            }
        }
    }

    @Override
    protected void harvest(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        BlockState state = world.getBlockState(pos);
        if (!helper.allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world)))
            return;
        Block block = state.getBlock();
        List<ItemStack> drops = mcjty.meecreeps.varia.BlockTools.getDrops(world, pos, state);
        SoundTools.playSound(world, state.getSoundType(world, pos, entity.getEntity()).getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        world.removeBlock(pos, false);
        boolean replanted = false;
        for (ItemStack stack : drops) {
            if ((!replanted) && stack.getItem() instanceof BlockItem) {
                BlockState plant = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                if (plant.getBlock() == state.getBlock()) {
                    // This is a valid seed
                    ItemStack seed = stack.split(1);
                    world.setBlock(pos, plant, 3);
                    replanted = true;
                }
            }
            ItemStack remaining = entity.addStack(stack);
            if (!remaining.isEmpty()) {
                helper.dropAndPutAwayLater(remaining);
            }
        }

        // If we didn't manage to get a seed from the drops we first check if we don't happen to have
        // a seed in our inventory so we can use that.
        for (ItemStack stack : entity.getInventory()) {
            if (replanted)
                break;
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                BlockState plant = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                if (plant.getBlock() == state.getBlock()) {
                    // This is a valid seed
                    ItemStack seed = stack.split(1);
                    world.setBlock(pos, plant, 3);
                    replanted = true;
                    break;
                }
            }
        }

        if (!replanted) {
            // We could not find any seed at all. Remember this so we can pick a seed from the chest next time
            needToReplant.put(pos, state.getBlock());
        }
    }

    private BlockPos hasSuitableSeed() {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        for (Map.Entry<BlockPos, Block> entry : needToReplant.entrySet()) {
            BlockPos pos = entry.getKey();
            Block block = entry.getValue();
            for (ItemStack stack : entity.getInventory()) {
                if (stack.getItem() instanceof BlockItem) {
                    BlockState plant = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
                    if (plant.getBlock() == block) {
                        // This is a valid seed
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        BlockPos pos;
        if (!needToReplant.isEmpty() && (pos = hasSuitableSeed()) != null) {
            helper.navigateTo(pos, this::replant);
        } else if (timeToWrapUp) {
            helper.done();
        } else {
            tryFindingCropsToHarvest();
        }
    }

}
