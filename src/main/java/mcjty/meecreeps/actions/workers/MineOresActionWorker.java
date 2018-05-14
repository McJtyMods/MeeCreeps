package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.ArrayList;
import java.util.List;

public class MineOresActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    public MineOresActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }

    protected void harvest(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester(world));
        SoundTools.playSound(world, block.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        world.setBlockToAir(pos);
        helper.giveDropsToMeeCreeps(drops);
    }


    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
        } else {
            tryFindingCropsToHarvest();
        }
    }

    protected void tryFindingCropsToHarvest() {
        IMeeCreep entity = helper.getMeeCreep();
        AxisAlignedBB box = getActionBox();
        World world = entity.getWorld();
        List<BlockPos> positions = new ArrayList<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> state.getBlock() == Blocks.FARMLAND && helper.allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world)),
                (pos, state) -> {
                    IBlockState cropState = world.getBlockState(pos.up());
                    Block cropBlock = cropState.getBlock();
                    boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(pos), world, pos, EnumFacing.UP, (IPlantable) cropBlock);
                    if (hasCrops) {
                        if (cropBlock instanceof BlockCrops) {
                            BlockCrops crops = (BlockCrops) cropBlock;
                            int age = crops.getAge(cropState);
                            int maxAge = crops.getMaxAge();
                            if (age >= maxAge) {
                                positions.add(pos.up());
                            }
                        } else if (cropBlock instanceof BlockNetherWart) {
                            int age = cropState.getValue(BlockNetherWart.AGE);
                            int maxAge = 3;
                            if (age >= maxAge) {
                                positions.add(pos.up());
                            }
                        }
                    }
                });
        if (!positions.isEmpty()) {
            BlockPos cropPos = positions.get(0);
            helper.navigateTo(cropPos, this::harvest);
        } else if (entity.hasStuffInInventory()) {
            helper.putStuffAway();
        }
    }

}
