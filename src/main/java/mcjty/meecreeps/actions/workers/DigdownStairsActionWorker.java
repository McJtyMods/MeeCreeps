package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.HashSet;
import java.util.Set;

public class DigdownStairsActionWorker extends AbstractActionWorker {
    private AxisAlignedBB actionBox = null;

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
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-20, -5, -20), options.getTargetPos().add(20, 5, 20));
        }
        return actionBox;
    }


    private boolean isSupportBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem ? DigTunnelActionWorker.isNotInterestedIn(((BlockItem) stack.getItem()).getBlock()) : false;
    }

    private void dig(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
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
                return p.up(1).offset(facing.rotateY());
            case 1:
                return p.up(1);
            case 2:
                return p.up(1).offset(facing.rotateYCCW());
            case 3:
                return p.offset(facing.rotateY());
            case 4:
                return p;
            case 5:
                return p.offset(facing.rotateYCCW());
            case 6:
                return p.down(1).offset(facing.rotateYCCW());
            case 7:
                return p.down(1);
            case 8:
                return p.down(1).offset(facing.rotateY());
            case 9:
                return p.up(2).offset(facing.rotateY());
            case 10:
                return p.up(2);
            case 11:
                return p.up(2).offset(facing.rotateYCCW());
            case 12:
                return p.up(3).offset(facing.rotateY());
            case 13:
                return p.up(3);
            case 14:
                return p.up(3).offset(facing.rotateYCCW());
        }
        return p;
    }

    private void buildSupport(BlockPos pos, ItemEntity entityItem) {
        IMeeCreep entity = helper.getMeeCreep();
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(1);
        if (blockStack.isEmpty()) {
            entityItem.remove();
        }
        if (actual.isEmpty()) {
            return;
        }
        Item item = actual.getItem();
        if (!(item instanceof BlockItem)) {
            // Safety
            return;
        }

        World world = entity.getWorld();
        Block block = ((BlockItem) item).getBlock();
        BlockState stateForPlacement = block.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(GeneralTools.getHarvester(world), Hand.MAIN_HAND, new BlockRayTraceResult(Vec3d.ZERO, Direction.UP, pos, false)))); // todo: see if we a proper trace here
        world.setBlockState(pos, stateForPlacement, 3);
        SoundTools.playSound(world, block.getSoundType(stateForPlacement).getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void buildStairs(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        numStairs--;
        World world = entity.getWorld();
        Block block = Blocks.STONE_STAIRS;
        BlockState stateForPlacement = block.getStateForPlacement(new BlockItemUseContext(new ItemUseContext(GeneralTools.getHarvester(world), Hand.MAIN_HAND, new BlockRayTraceResult(Vec3d.ZERO, Direction.UP, pos, false)))); // todo: see if we a proper trace here
        stateForPlacement = stateForPlacement.with(StairsBlock.FACING, getDirection().getOpposite());
        world.setBlockState(pos, stateForPlacement, 3);
        SoundTools.playSound(world, block.getSoundType(stateForPlacement).getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void collectCobble(ItemEntity entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.split(6);
        if (blockStack.isEmpty()) {
            entityItem.remove();
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
        return stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof StairsBlock;
    }

    private boolean isCobble(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() == Blocks.COBBLESTONE;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        if (timeToWrapUp) {
            if (numStairs > 0) {
                entity.getEntity().entityDropItem(new ItemStack(Blocks.STONE_STAIRS, numStairs), 0.0f);
                numStairs = 0;
            }
            if (numCobble > 0) {
                entity.getEntity().entityDropItem(new ItemStack(Blocks.COBBLESTONE, numCobble), 0.0f);
                numCobble = 0;
            }
            helper.done();
            return;
        }

        Direction facing = getDirection();

        BlockPos p = helper.getContext().getTargetPos().up().offset(facing, this.offset).down(this.offset+1);
        if (p.getY() < 6) {
            helper.taskIsDone();
            return;
        }

        if (checkSupports(facing, p)) {
            return;
        }

        BlockPos digpos = getBlockToDig(p, facing, blockidx);
        helper.navigateTo(p.offset(facing.getOpposite()), blockPos -> {
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
                        BlockPos position = entity.getEntity().getPosition();
                        AxisAlignedBB box = new AxisAlignedBB(position.add(-15, -8, -15), position.add(15, 8, 15));

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
            entityItem.remove();
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
        World world = entity.getWorld();
        if (!isStair(p.down(), world)) {
            buildStairs(p.down());
        } else if (!isStair(p.down().offset(facing.rotateY()), world)) {
            buildStairs(p.down().offset(facing.rotateY()));
        } else if (!isStair(p.down().offset(facing.rotateYCCW()), world)) {
            buildStairs(p.down().offset(facing.rotateYCCW()));
        }
    }

    private boolean needsStair() {
        return blockidx >= 6 && blockidx <= 8;
    }

    private boolean checkClear(BlockPos p, Direction facing) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        if (canDig(p, world)) {
            return false;
        }
        if (canDig(p.offset(facing.rotateY()), world)) {
            return false;
        }
        if (canDig(p.offset(facing.rotateYCCW()), world)) {
            return false;
        }
        if (canDig(p.up(), world)) {
            return false;
        }
        if (canDig(p.up().offset(facing.rotateY()), world)) {
            return false;
        }
        if (canDig(p.up().offset(facing.rotateYCCW()), world)) {
            return false;
        }
        if (canDig(p.up(2), world)) {
            return false;
        }
        if (canDig(p.up(2).offset(facing.rotateY()), world)) {
            return false;
        }
        if (canDig(p.up(2).offset(facing.rotateYCCW()), world)) {
            return false;
        }
        if (canDigOrStair(p.down(), world)) {
            return false;
        }
        if (canDigOrStair(p.down().offset(facing.rotateY()), world)) {
            return false;
        }
        if (canDigOrStair(p.down().offset(facing.rotateYCCW()), world)) {
            return false;
        }
        return true;
    }

    private boolean checkForStairs(BlockPos p, Direction facing) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        if (!isStair(p.down(), world)) {
            return false;
        }
        if (!isStair(p.down().offset(facing.rotateY()), world)) {
            return false;
        }
        if (!isStair(p.down().offset(facing.rotateYCCW()), world)) {
            return false;
        }
        return true;
    }

    private boolean canDig(BlockPos p, World world) {
        return !world.isAirBlock(p) && !positionsToSkip.contains(p);
    }

    private boolean canDigOrStair(BlockPos p, World world) {
        return !world.isAirBlock(p) && !positionsToSkip.contains(p) && !(world.getBlockState(p).getBlock() instanceof StairsBlock);
    }

    private boolean isStair(BlockPos p, World world) {
        return positionsToSkip.contains(p) || world.getBlockState(p).getBlock() instanceof StairsBlock;
    }

    private boolean checkSupports(Direction facing, BlockPos p) {
//        if (checkForSupport(p.down(2))) {
//            return true;
//        }
//        if (checkForSupport(p.down(2).offset(facing.rotateY()))) {
//            return true;
//        }
//        if (checkForSupport(p.down(2).offset(facing.rotateYCCW()))) {
//            return true;
//        }

        if (checkForLiquid(p.down(1).offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(1).offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(2).offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(3).offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.down(1).offset(facing.rotateYCCW(), 2))) {
            return true;
        }
        if (checkForLiquid(p.offset(facing.rotateYCCW(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(1).offset(facing.rotateYCCW(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(2).offset(facing.rotateYCCW(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(3).offset(facing.rotateYCCW(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(4))) {
            return true;
        }
        if (checkForLiquid(p.up(4).offset(facing.rotateY()))) {
            return true;
        }
        if (checkForLiquid(p.up(4).offset(facing.rotateYCCW()))) {
            return true;
        }
        return false;
    }

    private boolean checkForSupport(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        if (entity.getWorld().isAirBlock(p) || isLiquid(p)) {
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
        return block instanceof IFluidBlock || block instanceof FlowingFluidBlock;
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        offset = tag.getInt("offset");
        blockidx = tag.getInt("blockidx");
        numStairs = tag.getInt("stairs");
        numCobble = tag.getInt("cobble");
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        tag.putInt("offset", offset);
        tag.putInt("blockidx", blockidx);
        tag.putInt("stairs", numStairs);
        tag.putInt("cobble", numCobble);
    }
}
