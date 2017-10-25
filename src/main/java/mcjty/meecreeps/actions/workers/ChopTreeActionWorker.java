package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChopTreeActionWorker extends AbstractActionWorker {

    private static FakePlayer harvester = null;

    private List<BlockPos> blocks = new ArrayList<>();

    public ChopTreeActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        super(entity, options);
    }

    private void harvest(EntityMeeCreeps entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester());
        SoundTools.playSound(world, block.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        entity.getEntityWorld().setBlockToAir(pos);
        for (ItemStack stack : drops) {
            entity.entityDropItem(stack, 0.0f);
        }
    }

    private BlockPlanks.EnumType getWoodType(IBlockState state) {
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

    private void traverseTreeLogs(Set<BlockPos> alreadyDone, BlockPos pos, BlockPlanks.EnumType woodType) {
        alreadyDone.add(pos);
        blocks.add(pos);
        // @todo config
        if (blocks.size() > 100) {
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos p = pos.offset(facing);
            if (!alreadyDone.contains(p)) {
                IBlockState log = entity.getEntityWorld().getBlockState(p);
                if (log.getBlock() instanceof BlockOldLog || log.getBlock() instanceof BlockNewLog) {
                    if (woodType == getWoodType(log)) {
                        traverseTreeLogs(alreadyDone, p, woodType);
                    }
                }
            }
        }
    }

    private void findTree() {
        BlockPos startPos = options.getPos();

        // First find all logs of the same type
        IBlockState logBase = entity.getEntityWorld().getBlockState(startPos);
        BlockPlanks.EnumType woodType = getWoodType(logBase);
        Set<BlockPos> alreadyDone = new HashSet<>();
        traverseTreeLogs(alreadyDone, startPos, woodType);

        // Now find all leaves
    }

    @Override
    protected void performTick(boolean lastTask) {
        if (blocks.isEmpty()) {
            findTree();
        }
        if (blocks.isEmpty()) {
            // Nothing to do
            done();
            return;
        }

        if (lastTask) {
            done();
        } else if (!blocks.isEmpty()) {
            BlockPos p = blocks.remove(0);
            harvest(entity, p);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (BlockPos block : blocks) {
            list.appendTag(new NBTTagLong(block.toLong()));
        }
        tag.setTag("blocks", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        NBTTagList list = tag.getTagList("blocks", Constants.NBT.TAG_LONG);
        blocks.clear();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            blocks.add(BlockPos.fromLong(((NBTTagLong) list.get(i)).getLong()));
        }
    }
}
