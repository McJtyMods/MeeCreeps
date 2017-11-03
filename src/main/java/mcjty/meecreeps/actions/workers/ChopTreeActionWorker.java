package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.Counter;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class ChopTreeActionWorker extends AbstractActionWorker {

    protected List<BlockPos> blocks = new ArrayList<>();
    protected Counter<BlockPos> leavesToTick = new Counter<>();

    public ChopTreeActionWorker(EntityMeeCreeps entity, IActionOptions options) {
        super(entity, options);
    }

    private void harvest(BlockPos pos) {
        World world = entity.getEntityWorld();
        BlockPlanks.EnumType woodType = getWoodType(world.getBlockState(pos));
        harvestAndDrop(pos);
        findLeaves(pos, world, woodType);
    }

    protected void findLeaves(BlockPos pos, World world, BlockPlanks.EnumType woodType) {
        int offs = 4;
        for (int x = -offs; x <= offs; x++) {
            for (int y = -offs; y <= offs; y++) {
                for (int z = -offs; z <= offs; z++) {
                    BlockPos p = pos.add(x, y, z);
                    IBlockState st = world.getBlockState(p);
                    if (st.getBlock().isLeaves(st, world, p)) {
                        if (st.getValue(BlockLeaves.DECAYABLE)) {
                            if (getWoodType(st) == woodType) {
                                leavesToTick.put(p, 500);
                            }
                        }
                    }
                }
            }
        }
    }

    protected BlockPlanks.EnumType getWoodType(IBlockState state) {
        if (state.getBlock() instanceof BlockNewLog) {
            return state.getValue(BlockNewLog.VARIANT);
        } else if (state.getBlock() instanceof BlockOldLog) {
            return state.getValue(BlockOldLog.VARIANT);
        } else if (state.getBlock() instanceof BlockNewLeaf) {
            return state.getValue(BlockNewLeaf.VARIANT);
        } else if (state.getBlock() instanceof BlockOldLeaf) {
            return state.getValue(BlockOldLeaf.VARIANT);
        } else {
            return null;
        }
    }

    protected void traverseTreeLogs(Set<BlockPos> alreadyDone, BlockPos pos, BlockPlanks.EnumType woodType) {
        alreadyDone.add(pos);
        blocks.add(pos);
        // @todo config
        if (blocks.size() > 300) {
            return;
        }
        for (int y = -1 ; y <= 1 ; y++) {
            for (int x = -1 ; x <= 1 ; x++) {
                for (int z = -1 ; z <= 1 ; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos p = pos.add(x, y, z);
                        if (!alreadyDone.contains(p)) {
                            IBlockState log = entity.getEntityWorld().getBlockState(p);
                            if (allowedToHarvest(log, entity.getEntityWorld(), p, GeneralTools.getHarvester())) {
                                if (log.getBlock() instanceof BlockOldLog || log.getBlock() instanceof BlockNewLog) {
                                    if (woodType == getWoodType(log)) {
                                        traverseTreeLogs(alreadyDone, p, woodType);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void findTree() {
        BlockPos startPos = options.getTargetPos();
        if (entity.getEntityWorld().isAirBlock(startPos)) {
            return;
        }
        IBlockState logBase = entity.getEntityWorld().getBlockState(startPos);
        BlockPlanks.EnumType woodType = getWoodType(logBase);
        Set<BlockPos> alreadyDone = new HashSet<>();
        traverseTreeLogs(alreadyDone, startPos, woodType);
    }

    @Override
    protected void performTick(boolean timeToWrapUp) {
        if (blocks.isEmpty()) {
            findTree();
        }
        if (blocks.isEmpty() && leavesToTick.isEmpty()) {
            // There is nothing left to do
            done();
            return;
        }

        if (!leavesToTick.isEmpty()) {
            decayLeaves();
        }

        if (timeToWrapUp) {
            done();
        } else if (!blocks.isEmpty()) {
            harvest(blocks.remove(0));
            // @todo config
            waitABit = 5;   // Speed up things
        } else {
            taskIsDone();
        }
    }

    private void decayLeaves() {
        World world = entity.getEntityWorld();
        Counter<BlockPos> newmap = new Counter<>();
        for (Map.Entry<BlockPos, Integer> entry : leavesToTick.entrySet()) {
            BlockPos pos = entry.getKey();
            if (!world.isAirBlock(pos)) {
                IBlockState state = world.getBlockState(pos);
                state.getBlock().updateTick(world, pos, state, entity.getRNG());

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
