package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

public class DigdownStairsActionWorker extends AbstractActionWorker {
    private AABB actionBox = null;

    private int offset = 0;     // Offset from starting point
    private int blockidx = 0;
    private int numStairs = 0;
    private int numCobble = 0;

    private Direction direction = null;

    // We cannot break those so skip them
    private Set<BlockPos> positionsToSkip = new HashSet<BlockPos>();

    public DigdownStairsActionWorker(IWorkerHelper helper) {
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

    private Direction getDirection() {
        if (direction == null) {
            String id = options.getFurtherQuestionId();
            direction = Direction.byName(id);
        }
        return direction;
    }

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(-20, -5, -20)), net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(20, 5, 20)));
        }
        return actionBox;
    }

    private boolean isSupportBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem ? DigTunnelActionWorker.isNotInterestedIn(((BlockItem) stack.getItem()).getBlock()) : false;
    }

    private void dig(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        BlockState state = world.getBlockState(p);
        boolean result;
        if (DigTunnelActionWorker.isNotInterestedIn(state.getBlock())) {
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
            case 9:
                return p.above(2).relative(facing.getClockWise());
            case 10:
                return p.above(2);
            case 11:
                return p.above(2).relative(facing.getCounterClockWise());
            case 12:
                return p.above(3).relative(facing.getClockWise());
            case 13:
                return p.above(3);
            case 14:
                return p.above(3).relative(facing.getCounterClockWise());
        }
        return p;
    }

    private void buildSupport(BlockPos pos, ItemEntity entityItem) {
        IMeeCreep entity = helper.getMeeCreep();
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

        Level world = entity.getWorld();
        Block block = ((BlockItem) item).getBlock();
        BlockState stateForPlacement = mcjty.meecreeps.varia.BlockTools.placeStackAt(GeneralTools.getHarvester(world), actual, world, pos, Direction.UP);
        world.setBlock(pos, stateForPlacement, 3);
        SoundTools.playSound(world, stateForPlacement.getSoundType(world, pos, entity.getEntity()).getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void buildStairs(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        numStairs--;
        Level world = entity.getWorld();
        Block block = Blocks.STONE_STAIRS;
        BlockState stateForPlacement = block.defaultBlockState();
        stateForPlacement = stateForPlacement.setValue(StairBlock.FACING, getDirection().getOpposite());
        world.setBlock(pos, stateForPlacement, 3);
        SoundTools.playSound(world, stateForPlacement.getSoundType(world, pos, entity.getEntity()).getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void collectCobble(ItemEntity entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(6);
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
        numCobble += actual.getCount();
    }

    private boolean isStair(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof StairBlock;
    }

    private boolean isCobble(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() == Blocks.COBBLESTONE;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        if (timeToWrapUp) {
            if (numStairs > 0) {
                entity.getEntity().spawnAtLocation(new ItemStack(Blocks.STONE_STAIRS, numStairs), 0.0f);
                numStairs = 0;
            }
            if (numCobble > 0) {
                entity.getEntity().spawnAtLocation(new ItemStack(Blocks.COBBLESTONE, numCobble), 0.0f);
                numCobble = 0;
            }
            helper.done();
            return;
        }

        Direction facing = getDirection();

        BlockPos p = helper.getContext().getTargetPos().above().relative(facing, this.offset).below(this.offset + 1);
        if (p.getY() < entity.getWorld().getMinBuildHeight() + 6) {
            helper.taskIsDone();
            return;
        }

        if (checkSupports(facing, p)) {
            return;
        }

        BlockPos digpos = getBlockToDig(p, facing, blockidx);
        helper.navigateTo(p.relative(facing.getOpposite()), blockPos -> {
            helper.delayForHardBlocks(digpos, pp -> dig(digpos));
        });

        handleNextPosition(facing, p);
    }

    private void handleNextPosition(Direction facing, BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        blockidx++;
        if (blockidx >= 15) {
            blockidx = 14;      // Make sure we come here again next turn
            // Before we continue lets first see if things are ok
            if (checkClear(p, facing)) {
                if (checkForStairs(p, facing)) {
                    this.offset++;
                    blockidx = 0;
                } else {
                    // We still have to place down some stairs
                    if (entity.hasItem(this::isStair)) {
                        numStairs++;
                        entity.consumeItem(this::isStair, 1);
                    }
                    if (numCobble >= 6) {
                        // Craft stairs
                        numStairs += 4;
                        numCobble -= 6;
                    }
                    if (numStairs > 0) {
                        helper.navigateTo(p, blockPos -> placeStair(facing, p));
                    } else {
                        BlockPos position = entity.getEntity().blockPosition();
                        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(-15, -8, -15)), net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(15, 8, 15)));

                        if (!helper.findItemOnGround(box, this::isStair, entityItem -> placeStair(facing, p, entityItem))) {
                            // Collect cobble until we can make stairs
                            if (!helper.findItemOnGround(box, this::isCobble, this::collectCobble)) {
                                helper.showMessage("message.meecreeps.cant_find_stairs_or_cobble");
                            }
                        }
                    }
                }
            } else {
                // Restart here
                blockidx = 0;
            }
        }
    }

    private void placeStair(Direction facing, BlockPos pos, ItemEntity entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(32);
        numStairs += 32;
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

        placeStair(facing, pos);
    }

    private void placeStair(Direction facing, BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        if (!isStair(p.below(), world)) {
            buildStairs(p.below());
        } else if (!isStair(p.below().relative(facing.getClockWise()), world)) {
            buildStairs(p.below().relative(facing.getClockWise()));
        } else if (!isStair(p.below().relative(facing.getCounterClockWise()), world)) {
            buildStairs(p.below().relative(facing.getCounterClockWise()));
        }
    }

    private boolean needsStair() {
        return blockidx >= 6 && blockidx <= 8;
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
        if (canDig(p.above(2), world)) {
            return false;
        }
        if (canDig(p.above(2).relative(facing.getClockWise()), world)) {
            return false;
        }
        if (canDig(p.above(2).relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        if (canDigOrStair(p.below(), world)) {
            return false;
        }
        if (canDigOrStair(p.below().relative(facing.getClockWise()), world)) {
            return false;
        }
        if (canDigOrStair(p.below().relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        return true;
    }

    private boolean checkForStairs(BlockPos p, Direction facing) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        if (!isStair(p.below(), world)) {
            return false;
        }
        if (!isStair(p.below().relative(facing.getClockWise()), world)) {
            return false;
        }
        if (!isStair(p.below().relative(facing.getCounterClockWise()), world)) {
            return false;
        }
        return true;
    }

    private boolean canDig(BlockPos p, Level world) {
        return !world.isEmptyBlock(p) && !positionsToSkip.contains(p);
    }

    private boolean canDigOrStair(BlockPos p, Level world) {
        return !world.isEmptyBlock(p) && !positionsToSkip.contains(p) && !(world.getBlockState(p).getBlock() instanceof StairBlock);
    }

    private boolean isStair(BlockPos p, Level world) {
        return positionsToSkip.contains(p) || world.getBlockState(p).getBlock() instanceof StairBlock;
    }

    private boolean checkSupports(Direction facing, BlockPos p) {
//        if (checkForSupport(p.below(2))) {
//            return true;
//        }
//        if (checkForSupport(p.below(2).relative(facing.getClockWise()))) {
//            return true;
//        }
//        if (checkForSupport(p.below(2).relative(facing.getCounterClockWise()))) {
//            return true;
//        }

        if (checkForLiquid(p.below(1).relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(1).relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(2).relative(facing.getClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(3).relative(facing.getClockWise(), 2))) {
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
        if (checkForLiquid(p.above(2).relative(facing.getCounterClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(3).relative(facing.getCounterClockWise(), 2))) {
            return true;
        }
        if (checkForLiquid(p.above(4))) {
            return true;
        }
        if (checkForLiquid(p.above(4).relative(facing.getClockWise()))) {
            return true;
        }
        if (checkForLiquid(p.above(4).relative(facing.getCounterClockWise()))) {
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
        numStairs = tag.getInt("stairs");
        numCobble = tag.getInt("cobble");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("offset", offset);
        tag.putInt("blockidx", blockidx);
        tag.putInt("stairs", numStairs);
        tag.putInt("cobble", numCobble);
    }
}
