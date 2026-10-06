package mcjty.meecreeps.actions.workers;

import java.util.HashMap;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;

import java.util.*;

public class ChopTreeActionWorker extends AbstractActionWorker {

    protected List<BlockPos> blocks = new ArrayList<>();
    protected HashMap<BlockPos, Integer> leavesToTick = new HashMap<>();

    public ChopTreeActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        return null;
    }

    private void harvest(BlockPos pos) {
        Level world = helper.getMeeCreep().getWorld();
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

    protected void findLeaves(BlockPos pos, Level world) {
        int offs = 4;
        for (int x = -offs; x <= offs; x++) {
            for (int y = -offs; y <= offs; y++) {
                for (int z = -offs; z <= offs; z++) {
                    BlockPos p = pos.offset(x, y, z);
                    BlockState st = world.getBlockState(p);
                    if (st.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        if (st.getProperties().contains(LeavesBlock.PERSISTENT)) {
                            if (!st.getValue(LeavesBlock.PERSISTENT)) {
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
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos p = pos.offset(x, y, z);
                        if (!alreadyDone.contains(p)) {
                            BlockState log = entity.getWorld().getBlockState(p);
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
        if (entity.getWorld().isEmptyBlock(startPos)) {
            return;
        }
        BlockState logBase = entity.getWorld().getBlockState(startPos);
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
        Level world = entity.getWorld();
        HashMap<BlockPos, Integer> newmap = new HashMap<>();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!world.isEmptyBlock(pos)) {
                BlockState state = world.getBlockState(pos);
                state.randomTick((net.minecraft.server.level.ServerLevel) world, pos, entity.getRandom());

                if (!world.isEmptyBlock(pos)) {
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
    public void writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos block : blocks) {
            list.add(LongTag.valueOf(block.asLong()));
        }
        tag.put("blocks", list);

        list = new ListTag();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos block = entry.getKey();
            Integer counter = entry.getValue();
            CompoundTag tc = new CompoundTag();
            tc.putLong("p", block.asLong());
            tc.putInt("c", counter);
            list.add(tc);
        }
        tag.put("leaves", list);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        ListTag list = tag.getListOrEmpty("blocks");
        blocks.clear();
        for (int i = 0; i < list.size(); i++) {
            blocks.add(BlockPos.of(((LongTag) list.get(i)).longValue()));
        }
        list = tag.getListOrEmpty("leaves");
        leavesToTick.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tc = list.getCompoundOrEmpty(i);
            BlockPos pos = BlockPos.of(tc.getLongOr("p", 0L));
            int counter = tc.getIntOr("c", 0);
            leavesToTick.put(pos, counter);
        }
    }
}
