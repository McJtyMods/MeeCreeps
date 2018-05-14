package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockStaticLiquid;
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

public class DigTunnelActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

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
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-20, -5, -20), options.getTargetPos().add(20, 5, 20));
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
            b.add(Blocks.NETHER_BRICK);
            b.add(Blocks.END_STONE);
            b.add(Blocks.RED_SANDSTONE);
            b.add(Blocks.PURPUR_BLOCK);
            notInterestedInBlocks = b;
        }
        return notInterestedInBlocks.contains(block);
    }

    private boolean isSupportBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock ? isNotInterestedIn(((ItemBlock) stack.getItem()).getBlock()) : false;
    }

    private void dig(BlockPos p) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        IBlockState state = world.getBlockState(p);
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

    private BlockPos getBlockToDig(BlockPos p, EnumFacing facing, int blockidx) {
        switch (blockidx) {
            case 0: return p.up(1).offset(facing.rotateY());
            case 1: return p.up(1);
            case 2: return p.up(1).offset(facing.rotateYCCW());
            case 3: return p.offset(facing.rotateY());
            case 4: return p;
            case 5: return p.offset(facing.rotateYCCW());
            case 6: return p.down(1).offset(facing.rotateYCCW());
            case 7: return p.down(1);
            case 8: return p.down(1).offset(facing.rotateY());
        }
        return p;
    }

    private void buildSupport(BlockPos pos, EntityItem entityItem) {
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

        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        Block block = ((ItemBlock) item).getBlock();
        IBlockState stateForPlacement = block.getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, item.getMetadata(actual), GeneralTools.getHarvester(world), EnumHand.MAIN_HAND);
        world.setBlockState(pos, stateForPlacement, 3);
        SoundTools.playSound(world, block.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
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

        EnumFacing facing = helper.getContext().getTargetSide().getOpposite();
        // Target is bottom position but we need it to be at the center so that's why we do up()
        BlockPos p = helper.getContext().getTargetPos().up().offset(facing, this.offset);

        if (checkSupports(facing, p)) {
            return;
        }

        BlockPos torchPos = p.down().offset(facing.getOpposite());
        if (this.offset % 7 == 0 && !WorkerHelper.isTorch(entity.getWorld().getBlockState(torchPos).getBlock())) {
            // Time to place a torch if we have any
            if (entity.hasItem(WorkerHelper::isTorch)) {
                placeTorch(torchPos);
            }
        }

        BlockPos digpos = getBlockToDig(p, facing, blockidx);
        // Navigate to the block just adjacent to where we want to dig
        helper.navigateTo(p.offset(facing.getOpposite()), blockPos -> {
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
        if (canDig(p.down(), world)) {
            return false;
        }
        if (canDig(p.down().offset(facing.rotateY()), world)) {
            return false;
        }
        if (canDig(p.down().offset(facing.rotateYCCW()), world)) {
            return false;
        }
        return true;
    }

    private boolean canDig(BlockPos p, World world) {
        return !world.isAirBlock(p) && !positionsToSkip.contains(p);
    }

    private boolean checkSupports(EnumFacing facing, BlockPos p) {
        if (checkForSupport(p.down(2))) {
            return true;
        }
        if (checkForSupport(p.down(2).offset(facing.rotateY()))) {
            return true;
        }
        if (checkForSupport(p.down(2).offset(facing.rotateYCCW()))) {
            return true;
        }

        if (checkForLiquid(p.down(1).offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.offset(facing.rotateY(), 2))) {
            return true;
        }
        if (checkForLiquid(p.up(1).offset(facing.rotateY(), 2))) {
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
        if (checkForLiquid(p.up(2))) {
            return true;
        }
        if (checkForLiquid(p.up(2).offset(facing.rotateY()))) {
            return true;
        }
        if (checkForLiquid(p.up(2).offset(facing.rotateYCCW()))) {
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
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("offset", offset);
        tag.setInteger("blockidx", blockidx);
    }
}
