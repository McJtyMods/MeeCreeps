package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class DigdownActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;
    private BlockPos supportPosTodo = null;

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(-10, -5, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(10, 5, 10)));
        }
        return actionBox;
    }

    public DigdownActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private boolean isLadder(ItemStack stack) {
        return stack.getItem() == Item.byBlock(Blocks.LADDER);
    }

    private void placeLadder(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        ItemStack ladder = entity.consumeItem(this::isLadder, 1);
        if (!ladder.isEmpty()) {
            world.setBlock(pos, Blocks.LADDER.defaultBlockState(), 3);
            SoundTools.playSound(world, Blocks.LADDER.defaultBlockState().getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        }
    }

    private BlockPos findTopSpotNotDiggedYet() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos p = options.getTargetPos();
        Level world = entity.getWorld();
        BlockState state = world.getBlockState(p);
        while (p.getY() > world.getMinY() && (world.isEmptyBlock(p) || state.getBlock() == Blocks.LADDER)) {
            p = p.below();
            state = world.getBlockState(p);
        }
        return p;
    }

    private void digDown() {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        BlockPos p = findTopSpotNotDiggedYet();
        BlockState state = world.getBlockState(p);
        if (p.getY() <= world.getMinY() || world.isEmptyBlock(p) || state.getBlock() == Blocks.LADDER) {
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
            Level world = entity.getWorld();
            int y = p.getY();
            while (y < options.getTargetPos().getY()) {
                BlockPos test = new BlockPos(p.getX(), y, p.getZ());
                if (world.isEmptyBlock(test.south())) {
                    supportPosTodo = test;
                    return true;
                }
                y++;
            }
        }
        return false;
    }

    private void buildSupportBlock(ItemEntity entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(1);
        if (blockStack.isEmpty()) {
            entityItem.discard();
        }
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();

        Block block = ((BlockItem) actual.getItem()).getBlock();
        BlockState stateForPlacement = mcjty.meecreeps.varia.BlockTools.placeStackAt(GeneralTools.getHarvester(world), actual, world, supportPosTodo, Direction.UP);
        world.setBlock(supportPosTodo.south(), stateForPlacement, 3);
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
            b.add(Blocks.NETHER_BRICKS);
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
            if (item instanceof BlockItem) {
                BlockItem itemBlock = (BlockItem) item;
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
            if (!helper.findItemOnGround(new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(entity.getEntity().blockPosition().offset(-3, -3, -3)), net.minecraft.world.phys.Vec3.atLowerCornerOf(entity.getEntity().blockPosition().offset(3, 3, 3))),
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
