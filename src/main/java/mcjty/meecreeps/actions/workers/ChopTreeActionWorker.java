package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.Counter;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class ChopTreeActionWorker extends AbstractActionWorker {

    protected List<BlockPos> blocks = new ArrayList<>();
    protected Counter<BlockPos> leavesToTick = new Counter<>();

    public ChopTreeActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AxisAlignedBB getActionBox() {
        return null;
    }

    private void harvest(BlockPos pos) {
        World world = helper.getMeeCreep().getWorld();
        helper.harvestAndDrop(pos);
        findLeaves(pos, world);
    }

    @Override
    public void init(IMeeCreep meeCreep) {
        helper.setSpeed(5);
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    protected void findLeaves(BlockPos pos, World world) {
        int offs = 4;
        for (int x = -offs; x <= offs; x++) {
            for (int y = -offs; y <= offs; y++) {
                for (int z = -offs; z <= offs; z++) {
                    BlockPos p = pos.add(x, y, z);
                    IBlockState st = world.getBlockState(p);
                    if (st.getBlock().isLeaves(st, world, p)) {
                        if (st.getPropertyKeys().contains(BlockLeaves.DECAYABLE)) {
                            if (st.getValue(BlockLeaves.DECAYABLE)) {
                                leavesToTick.put(p, 500);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void traverseTreeLogs(Set<BlockPos> alreadyDone, BlockPos pos, Block woodBlock) {
        alreadyDone.add(pos);
        blocks.add(pos);
        if (blocks.size() > ConfigSetup.maxTreeBlocks.get()) {
            return;
        }
        IMeeCreep entity = helper.getMeeCreep();
        for (int y = -1 ; y <= 1 ; y++) {
            for (int x = -1 ; x <= 1 ; x++) {
                for (int z = -1 ; z <= 1 ; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos p = pos.add(x, y, z);
                        if (!alreadyDone.contains(p)) {
                            IBlockState log = entity.getWorld().getBlockState(p);
                            if (helper.allowedToHarvest(log, entity.getWorld(), p, GeneralTools.getHarvester(entity.getWorld()))) {
                                if (log.getBlock() == woodBlock) {
                                    traverseTreeLogs(alreadyDone, p, woodBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void findTree() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos startPos = options.getTargetPos();
        if (entity.getWorld().isAirBlock(startPos)) {
            return;
        }
        IBlockState logBase = entity.getWorld().getBlockState(startPos);
        Set<BlockPos> alreadyDone = new HashSet<>();
        traverseTreeLogs(alreadyDone, startPos, logBase.getBlock());
    }

    @Override
    public void tick(boolean timeToWrapUp) {

        if (timeToWrapUp) {
            helper.done();
            return;
        }

        if (blocks.isEmpty()) {
            findTree();
        }
        if (blocks.isEmpty() && leavesToTick.isEmpty()) {
            // There is nothing left to do
            helper.done();
            return;
        }

        if (!leavesToTick.isEmpty()) {
            decayLeaves();
        }

        if (!blocks.isEmpty()) {
            BlockPos toRemove = blocks.remove(0);
            helper.navigateTo(options.getTargetPos(), blockPos -> harvest(toRemove));
        } else {
            helper.taskIsDone();
        }
    }

    private void decayLeaves() {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        Counter<BlockPos> newmap = new Counter<>();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!world.isAirBlock(pos)) {
                IBlockState state = world.getBlockState(pos);
                state.getBlock().updateTick(world, pos, state, entity.getRandom());

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
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (BlockPos block : blocks) {
            list.appendTag(new NBTTagLong(block.toLong()));
        }
        tag.setTag("blocks", list);

        list = new NBTTagList();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos block = entry.getKey();
            Integer counter = entry.getValue();
            NBTTagCompound tc = new NBTTagCompound();
            tc.setLong("p", block.toLong());
            tc.setInteger("c", counter);
            list.appendTag(tc);
        }
        tag.setTag("leaves", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("blocks", Constants.NBT.TAG_LONG);
        blocks.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            blocks.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
        list = tag.getTagList("leaves", Constants.NBT.TAG_COMPOUND);
        leavesToTick.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(tc.getLong("p"));
            int counter = tc.getInteger("c");
            leavesToTick.put(pos, counter);
        }
    }
}
