package mcjty.meecreeps.actions.workers;

import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        NonNullList<ItemStack> drops = NonNullList.create();
        drops.addAll(block.getDrops(state, (ServerWorld) world, pos, world.getTileEntity(pos)));

        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester(world));
        SoundTools.playSound(world, block.getSoundType(state).getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
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
                    BlockState cropState = world.getBlockState(pos.up());
                    Block cropBlock = cropState.getBlock();
                    boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(pos), world, pos, Direction.UP, (IPlantable) cropBlock);
                    if (hasCrops) {
                        if (cropBlock instanceof CropsBlock) {
                            CropsBlock crops = (CropsBlock) cropBlock;
                            int age = cropState.get(crops.getAgeProperty());
                            int maxAge = crops.getMaxAge();
                            if (age >= maxAge) {
                                positions.add(pos.up());
                            }
                        } else if (cropBlock instanceof NetherWartBlock) {
                            int age = cropState.get(NetherWartBlock.AGE);
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
