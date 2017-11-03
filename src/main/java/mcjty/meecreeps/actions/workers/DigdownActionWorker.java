package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
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
    protected AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }

    public DigdownActionWorker(EntityMeeCreeps entity, IActionOptions options) {
        super(entity, options);
    }

    private boolean isLadder(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.LADDER);
    }

    private void placeLadder(BlockPos pos) {
        World world = entity.getEntityWorld();
        ItemStack ladder = entity.consumeItem(this::isLadder, 1);
        if (!ladder.isEmpty()) {
            entity.getEntityWorld().setBlockState(pos, Blocks.LADDER.getDefaultState(), 3);
            SoundTools.playSound(world, Blocks.LADDER.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        }
    }

    private BlockPos findTopSpotNotDiggedYet() {
        BlockPos p = options.getTargetPos();
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(p);
        while (world.isAirBlock(p) || state.getBlock() == Blocks.LADDER) {
            p = p.down();
            state = world.getBlockState(p);
        }
        return p;
    }

    private void digDown() {
        World world = entity.getEntityWorld();
        BlockPos p = findTopSpotNotDiggedYet();
        IBlockState state = world.getBlockState(p);
        if (state.getBlock().getBlockHardness(state, world, p) >= 0 && allowedToHarvest(state, world, p, GeneralTools.getHarvester())) {
            harvestAndDrop(p);
            placeLadder(p);
        } else {
            // Too hard or not allowed. We stop here
            taskIsDone();
        }
    }

    private boolean needsSupportPillar(BlockPos p) {
        supportPosTodo = null;
        if (p.getY() < options.getTargetPos().getY()) {
            World world = entity.getEntityWorld();
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
        World world = entity.getEntityWorld();

        Block block = ((ItemBlock) actual.getItem()).getBlock();
        IBlockState stateForPlacement = block.getStateForPlacement(world, supportPosTodo.south(), EnumFacing.DOWN, 0, 0, 0, actual.getItem().getMetadata(actual), GeneralTools.getHarvester(), EnumHand.MAIN_HAND);
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
    protected void performTick(boolean timeToWrapUp) {
        if (supportPosTodo != null) {
            if (!findItemOnGround(new AxisAlignedBB(entity.getPosition().add(-3, -3, -3), entity.getPosition().add(3, 3, 3)),
                    this::isBuildingBlock, this::buildSupportBlock)) {
                // We didn't find a suitable item to build support with. If it is time to wrap up
                // then we will not find any suitable blocks later so we just stop then
                if (timeToWrapUp) {
                    done();
                    return;
                }
            }
        }

        if (timeToWrapUp && supportPosTodo == null) {
            done();
        } else if (!entity.hasItem(this::isLadder)) {
            findItemOnGroundOrInChest(this::isLadder, "I cannot find any ladders");
        } else {
            BlockPos p = findTopSpotNotDiggedYet();
            if (!needsSupportPillar(p)) {
                navigateTo(p, blockPos -> digDown());
            }
        }
    }
}
