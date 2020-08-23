package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.HarvestReplantActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.block.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;

public class HarvestReplantActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, Direction side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));

        for (double x = box.minX ; x <= box.maxX ; x++) {
            for (double y = box.minY ; y <= box.maxY ; y++) {
                for (double z = box.minZ ; z <= box.maxZ ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (state.getBlock() == Blocks.FARMLAND) {
                        BlockState cropState = world.getBlockState(p.up());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(p), world, p, Direction.UP, (IPlantable) cropBlock);
                        if (hasCrops) {
                            if (cropBlock instanceof CropsBlock) {
                                CropsBlock crops = (CropsBlock) cropBlock;
                                int age = cropState.get(crops.getAgeProperty());
                                int maxAge = crops.getMaxAge();
                                if (age >= maxAge) {
                                    return true;
                                }
                            } else if (cropBlock instanceof NetherWartBlock) {
                                int age = cropState.get(NetherWartBlock.AGE);
                                int maxAge = 3;
                                if (age >= maxAge) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean isPossibleSecondary(World world, BlockPos pos, Direction side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));

        for (double x = box.minX ; x <= box.maxX ; x++) {
            for (double y = box.minY ; y <= box.maxY ; y++) {
                for (double z = box.minZ ; z <= box.maxZ ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (state.getBlock() == Blocks.FARMLAND) {
                        BlockState cropState = world.getBlockState(p.up());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(p), world, p, Direction.UP, (IPlantable) cropBlock);
                        if (hasCrops) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public IActionWorker createWorker(@Nonnull IWorkerHelper helper) {
        return new HarvestReplantActionWorker(helper);
    }
}
