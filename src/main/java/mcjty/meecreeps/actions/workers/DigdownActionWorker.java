package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class DigdownActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;
    private BlockPos supportPosTodo = null;

    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }

    public DigdownActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private boolean isLadder(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.LADDER);
    }

    private void placeLadder(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        ItemStack ladder = entity.consumeItem(this::isLadder, 1);
        if (!ladder.isEmpty()) {
            world.setBlockState(pos, Blocks.LADDER.getDefaultState(), 3);
            SoundTools.playSound(world, Blocks.LADDER.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        }
    }

    private BlockPos findTopSpotNotDiggedYet() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos p = options.getTargetPos();
        World world = entity.getWorld();
        IBlockState state = world.getBlockState(p);
        while (p.getY() > 0 && (world.isAirBlock(p) || state.getBlock() == Blocks.LADDER)) {
            p = p.down();
            state = world.getBlockState(p);
        }
        return p;
    }

    private void digDown() {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        BlockPos p = findTopSpotNotDiggedYet();
        IBlockState state = world.getBlockState(p);
        if (p.getY() < 1 || world.isAirBlock(p) || state.getBlock() == Blocks.LADDER) {
            helper.taskIsDone();
        } else if (helper.allowedToHarvest(state, world, p, GeneralTools.getHarvester(world))) {
            helper.delayForHardBlocks(p, pp -> {
                if (helper.harvestAndDrop(p)) {
                    placeLadder(p);
                } else {
                    // Too hard or not allowed. We stop here
                    helper.taskIsDone();
                }
            });
        }
    }

    private boolean needsSupportPillar(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        supportPosTodo = null;
        if (p.getY() < options.getTargetPos().getY()) {
            World world = entity.getWorld();
            int y = p.getY();
            while (y < options.getTargetPos().getY()) {
                BlockPos test = new BlockPos(p.getX(), y, p.getZ());
                if (world.isAirBlock(test.south())) {
                    supportPosTodo = test;
                    return true;
                }
                y++;
            }
        }
        return false;
    }

    private void buildSupportBlock(EntityItem entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.splitStack(1);
        if (blockStack.isEmpty()) {
            entityItem.setDead();
        }
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();

        Block block = ((ItemBlock) actual.getItem()).getBlock();
        IBlockState stateForPlacement = block.getStateForPlacement(world, supportPosTodo.south(), EnumFacing.DOWN, 0, 0, 0, actual.getItem().getMetadata(actual), GeneralTools.getHarvester(world), EnumHand.MAIN_HAND);
        world.setBlockState(supportPosTodo.south(), stateForPlacement, 3);
        placeLadder(supportPosTodo);
        supportPosTodo = null;
    }

    private static Set<Block> buildingBlocks = null;

    private static boolean isBuildingBlock(Block block) {
        if (buildingBlocks == null) {
            Set<Block> b = new HashSet<>();
            b.add(Blocks.STONE);
            b.add(Blocks.COBBLESTONE);
            b.add(Blocks.DIRT);
            b.add(Blocks.SANDSTONE);
            b.add(Blocks.NETHERRACK);
            b.add(Blocks.NETHER_BRICK);
            b.add(Blocks.END_STONE);
            b.add(Blocks.RED_SANDSTONE);
            b.add(Blocks.PURPUR_BLOCK);
            buildingBlocks = b;
        }
        return buildingBlocks.contains(block);
    }

    private boolean isBuildingBlock(ItemStack stack) {
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) item;
                if (isBuildingBlock(itemBlock.getBlock())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();

        if (supportPosTodo != null) {
            if (!helper.findItemOnGround(new AxisAlignedBB(entity.getEntity().getPosition().add(-3, -3, -3), entity.getEntity().getPosition().add(3, 3, 3)),
                    this::isBuildingBlock, this::buildSupportBlock)) {
                // We didn't find a suitable item to build support with. If it is time to wrap up
                // then we will not find any suitable blocks later so we just stop then
                if (timeToWrapUp) {
                    helper.done();
                    return;
                }
            }
        }

        if (timeToWrapUp && supportPosTodo == null) {
            helper.done();
        } else if (!entity.hasItem(this::isLadder)) {
            helper.findItemOnGroundOrInChest(this::isLadder, 128, "message.meecreeps.cant_find_ladders"); // At most 2 stacks
        } else {
            BlockPos p = findTopSpotNotDiggedYet();
            if (!needsSupportPillar(p)) {
                helper.navigateTo(p, blockPos -> digDown());
            }
        }
    }
}
