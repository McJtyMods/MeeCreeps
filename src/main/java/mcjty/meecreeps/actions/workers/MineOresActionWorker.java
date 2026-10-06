package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.varia.SoundTools;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class MineOresActionWorker extends AbstractActionWorker {

    private AABB actionBox = null;

    public MineOresActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(-10, -5, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(options.getTargetPos().offset(10, 5, 10)));
        }
        return actionBox;
    }

    protected void harvest(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = mcjty.meecreeps.varia.BlockTools.getDrops(world, pos, state);
        SoundTools.playSound(world, state.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        world.removeBlock(pos, false);
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
        AABB box = getActionBox();
        Level world = entity.getWorld();
        List<BlockPos> positions = new ArrayList<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> state.getBlock() == Blocks.FARMLAND && helper.allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world)),
                (pos, state) -> {
                    BlockState cropState = world.getBlockState(pos.above());
                    Block cropBlock = cropState.getBlock();
                    boolean hasCrops = (cropBlock instanceof CropBlock || cropBlock instanceof NetherWartBlock)
                                && cropState.canSurvive(world, pos.above());
                    if (hasCrops) {
                        if (cropBlock instanceof CropBlock) {
                            CropBlock crops = (CropBlock) cropBlock;
                            int age = crops.getAge(cropState);
                            int maxAge = crops.getMaxAge();
                            if (age >= maxAge) {
                                positions.add(pos.above());
                            }
                        } else if (cropBlock instanceof NetherWartBlock) {
                            int age = cropState.getValue(NetherWartBlock.AGE);
                            int maxAge = 3;
                            if (age >= maxAge) {
                                positions.add(pos.above());
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
