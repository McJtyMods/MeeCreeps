package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class DigTunnelActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;

    private int offset = 0;     // Offset from starting point
    private int blockidx = 0;

    private int torchChecker = 40;
    // We cannot break those so skip them
    private Set<BlockPos> positionsToSkip = new HashSet<>();


    public DigTunnelActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public void init(IMeeCreep meeCreep) {
        helper.setSpeed(5);
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(options.getTargetPos().offset(-20, -5, -20), options.getTargetPos().offset(20, 5, 20));
        }
        return actionBox;
    }

    private static Set<Block> notInterestedInBlocks = null;

    public static boolean isNotInterestedIn(Block block) {
        if (notInterestedInBlocks == null) {
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
            notInterestedInBlocks = b;
        }
        return notInterestedInBlocks.contains(block);
    }

    private boolean isSupportBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem ? isNotInterestedIn(((BlockItem) stack.getItem()).getBlock()) : false;
    }

    private void dig(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        BlockState state = world.getBlockState(p);
        boolean result;
        if (isNotInterestedIn(state.getBlock())) {
            result = helper.harvestAndDrop(p);
        } else {
            result = helper.harvestAndPickup(p);
        }
        if (!result) {
            // Too hard or not allowed. Skip it
            positionsToSkip.add(p);
        }
    }

    private BlockPos getBlockToDig(BlockPos p, Direction facing, int blockidx) {
        switch (blockidx) {
            case 0:
                return p.above(1).relative(facing.getClockWise());
            case 1:
                return p.above(1);
            case 2:
                return p.above(1).relative(facing.getCounterClockWise());
            case 3:
                return p.relative(facing.getClockWise());
            case 4:
                return p;
            case 5:
                return p.relative(facing.getCounterClockWise());
            case 6:
                return p.below(1).relative(facing.getCounterClockWise());
            case 7:
                return p.below(1);
            case 8:
                return p.below(1).relative(facing.getClockWise());
        }
        return p;
    }

    private void buildSupport(BlockPos pos, ItemEntity entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(1);
        if (blockStack.isEmpty()) {
            entityItem.discard();
        }
        if (actual.isEmpty()) {
            return;
        }
        Item item = actual.getItem();
        if (!(item instanceof BlockItem)) {
            // Safety
            return;
        }

        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        Block block = ((BlockItem) item).getBlock();
        BlockState stateForPlacement = mcjty.meecreeps.varia.BlockTools.placeStackAt(GeneralTools.getHarvester(world), actual, world, pos, Direction.UP);
        world.setBlock(pos, stateForPlacement, 3);
        SoundTools.playSound(world, stateForPlacement.getSoundType(world, pos, entity.getEntity()).getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        ItemStack torch = entity.consumeItem(WorkerHelper::isTorch, 1);
        if (!torch.isEmpty()) {
            helper.placeStackAt(torch, world, pos);
        }
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        if (timeToWrapUp) {
            helper.done();
            return;
        }

        // Don't check for torches every time. That's too expensive
        torchChecker--;
        if (torchChecker <= 0) {
            torchChecker = 40;
            if (!entity.hasItem(WorkerHelper::isTorch)) {
                if (helper.findItemOnGroundOrInChest(WorkerHelper::isTorch, 64)) {
                    // Lets first handle the fetching of the torches
                    return;
                }
            }
        }

        Direction facing = helper.getContext().getTargetSide().getOpposite();
        // Target is bottom position but we need it to be at the center so that's why we do up()
        BlockPos p = helper.getContext().getTargetPos().above().relative(facing, this.offset);

        if (checkSupports(facing, p)) {
            return;
        }

        BlockPos torchPos = p.below().relative(facing.getOpposite());
        if (this.offset % 7 == 0 && !WorkerHelper.isTorch(entity.getWorld().getBlockState(torchPos).getBlock())) {
            // Time to place a torch if we have any
            if (entity.hasItem(WorkerHelper::isTorch)) {
                placeTorch(torchPos);
            }
        }

        BlockPos digpos = getBlockToDig(p, facing, blockidx);
        // Navigate to the block just adjacent to where we want to dig
        helper.navigateTo(p.relative(facing.getOpposite()), blockPos -> {
            helper.delayForHardBlocks(digpos, pp -> dig(digpos));
        });

        blockidx++;
        if (blockidx >= 9) {
            // Before we continue lets first see if things are ok
            if (checkClear(p, facing)) {
                this.offset++;
                blockidx = 0;
                if (this.offset >= 32) {
                    helper.taskIsDone();
                }
            } else {
                // Restart here
                blockidx = 0;
            }
        }
    }

    private boolean checkClear(BlockPos p, Direction facing) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        if (canDig(p, world)) {
            return false;
        }
        if (canDig(p.relative(facing.getClockWise()), world)) {
            return false;
        }
        if (canDig(p.relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        if (canDig(p.above(), world)) {
            return false;
        }
        if (canDig(p.above().relative(facing.getClockWise()), world)) {
            return false;
        }
        if (canDig(p.above().relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        if (canDig(p.below(), world)) {
            return false;
        }
        if (canDig(p.below().relative(facing.getClockWise()), world)) {
            return false;
        }
        if (canDig(p.below().relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        return true;
    }

    private boolean canDig(BlockPos p, Level world) {
        return !world.isEmptyBlock(p) && !positionsToSkip.contains(p);
    }

    private boolean checkSupports(Direction facing, BlockPos p) {
        if (checkForSupport(p.below(2))) {
            return true;
        }
        if (checkForSupport(p.below(2).relative(facing.getClockWise()))) {
            return true;
        }
        if (checkForSupport(p.below(2).relative(facing.getCounterClockWise()))) {
            return true;
        }

        if (checkForLiquid(p.below(1).relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(1).relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.below(1).relative(facing.getCounterClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.relative(facing.getCounterClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(1).relative(facing.getCounterClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(2))) {
            return true;
        }
        if (checkForLiquid(p.above(2).relative(facing.getClockWise()))) {
            return true;
        }
        if (checkForLiquid(p.above(2).relative(facing.getCounterClockWise()))) {
            return true;
        }
        return false;
    }

    private boolean checkForSupport(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        if (entity.getWorld().isEmptyBlock(p) || isLiquid(p)) {
            if (!helper.findItemOnGround(getSearchBox(), this::isSupportBlock, entityItem -> buildSupport(p, entityItem))) {
                // We cannot continu like this
                helper.showMessage("message.meecreeps.cant_continue");
                helper.taskIsDone();
            }
            return true;
        }
        return false;
    }

    private boolean checkForLiquid(BlockPos p) {
        if (isLiquid(p)) {
            if (!helper.findItemOnGround(getSearchBox(), this::isSupportBlock, entityItem -> buildSupport(p, entityItem))) {
                // We cannot continue like this
                helper.showMessage("message.meecreeps.cant_continue");
                helper.taskIsDone();
            }
            return true;
        }
        return false;
    }

    private boolean isLiquid(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        Block block = entity.getWorld().getBlockState(p).getBlock();
        return block instanceof LiquidBlock;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        offset = tag.getInt("offset");
        blockidx = tag.getInt("blockidx");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("offset", offset);
        tag.putInt("blockidx", blockidx);
    }
}
