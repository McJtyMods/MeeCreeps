package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class DigdownStairsActionWorker extends AbstractActionWorker {
    private AxisAlignedBB actionBox = null;

    private int offset = 0;     // Offset from starting point
    private int blockidx = 0;
    private int numStairs = 0;
    private int numCobble = 0;

    private EnumFacing direction = null;

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

    private EnumFacing getDirection() {
        if (direction == null) {
            String id = options.getFurtherQuestionId();
            direction = EnumFacing.byName(id);
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
        return stack.getItem() instanceof ItemBlock ? DigTunnelActionWorker.isNotInterestedIn(((ItemBlock) stack.getItem()).getBlock()) : false;
    }

    private void dig(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        IBlockState state = world.getBlockState(p);
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

    private BlockPos getBlockToDig(BlockPos p, EnumFacing facing, int blockidx) {
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

    private void buildSupport(BlockPos pos, EntityItem entityItem) {
        IMeeCreep entity = helper.getMeeCreep();
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.splitStack(1);
        if (blockStack.isEmpty()) {
            entityItem.setDead();
        }
        if (actual.isEmpty()) {
            return;
        }
        Item item = actual.getItem();
        if (!(item instanceof ItemBlock)) {
            // Safety
            return;
        }

        World world = entity.getWorld();
        Block block = ((ItemBlock) item).getBlock();
        IBlockState stateForPlacement = block.getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, item.getMetadata(actual), GeneralTools.getHarvester(world), EnumHand.MAIN_HAND);
        world.setBlockState(pos, stateForPlacement, 3);
        SoundTools.playSound(world, block.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void buildStairs(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        numStairs--;
        World world = entity.getWorld();
        Block block = Blocks.STONE_STAIRS;
        IBlockState stateForPlacement = block.getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, getDirection().getOpposite().ordinal()-2, GeneralTools.getHarvester(world), EnumHand.MAIN_HAND);
        stateForPlacement = stateForPlacement.withProperty(BlockStairs.FACING, getDirection().getOpposite());
        world.setBlockState(pos, stateForPlacement, 3);
        SoundTools.playSound(world, block.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void collectCobble(EntityItem entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.splitStack(6);
        if (blockStack.isEmpty()) {
            entityItem.setDead();
        }
        if (actual.isEmpty()) {
            return;
        }
        Item item = actual.getItem();
        if (!(item instanceof ItemBlock)) {
            // Safety
            return;
        }
        numCobble += actual.getCount();
    }

    private boolean isStair(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() instanceof BlockStairs;
    }

    private boolean isCobble(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == Blocks.COBBLESTONE;
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

        EnumFacing facing = getDirection();

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

    private void handleNextPosition(EnumFacing facing, BlockPos p) {
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

    private void placeStair(EnumFacing facing, BlockPos pos, EntityItem entityItem) {
        ItemStack blockStack = entityItem.getItem();
        ItemStack actual = blockStack.splitStack(32);
        numStairs += 32;
        if (blockStack.isEmpty()) {
            entityItem.setDead();
        }
        if (actual.isEmpty()) {
            return;
        }
        Item item = actual.getItem();
        if (!(item instanceof ItemBlock)) {
            // Safety
            return;
        }

        placeStair(facing, pos);
    }

    private void placeStair(EnumFacing facing, BlockPos p) {
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

    private boolean checkClear(BlockPos p, EnumFacing facing) {
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

    private boolean checkForStairs(BlockPos p, EnumFacing facing) {
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
        return !world.isAirBlock(p) && !positionsToSkip.contains(p) && !(world.getBlockState(p).getBlock() instanceof BlockStairs);
    }

    private boolean isStair(BlockPos p, World world) {
        return positionsToSkip.contains(p) || world.getBlockState(p).getBlock() instanceof BlockStairs;
    }

    private boolean checkSupports(EnumFacing facing, BlockPos p) {
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
        return block instanceof BlockLiquid || block instanceof BlockDynamicLiquid || block instanceof BlockStaticLiquid;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        offset = tag.getInteger("offset");
        blockidx = tag.getInteger("blockidx");
        numStairs = tag.getInteger("stairs");
        numCobble = tag.getInteger("cobble");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("offset", offset);
        tag.setInteger("blockidx", blockidx);
        tag.setInteger("stairs", numStairs);
        tag.setInteger("cobble", numCobble);
    }
}
