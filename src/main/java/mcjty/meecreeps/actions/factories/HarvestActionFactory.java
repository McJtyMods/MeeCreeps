package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.HarvestActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nonnull;

public class HarvestActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos, EnumFacing side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));

        for (double x = box.minX ; x <= box.maxX ; x++) {
            for (double y = box.minY ; y <= box.maxY ; y++) {
                for (double z = box.minZ ; z <= box.maxZ ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(p);
                    if (state.getBlock() == Blocks.FARMLAND) {
                        IBlockState cropState = world.getBlockState(p.up());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(p), world, p, EnumFacing.UP, (IPlantable) cropBlock);
                        if (hasCrops) {
                            if (cropBlock instanceof BlockCrops) {
                                BlockCrops crops = (BlockCrops) cropBlock;
                                int age = crops.getAge(cropState);
                                int maxAge = crops.getMaxAge();
                                if (age >= maxAge) {
                                    return true;
                                }
                            } else if (cropBlock instanceof BlockNetherWart) {
                                BlockNetherWart wart = (BlockNetherWart) cropBlock;
                                int age = cropState.getValue(BlockNetherWart.AGE);
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
    public boolean isPossibleSecondary(World world, BlockPos pos, EnumFacing side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AxisAlignedBB box = new AxisAlignedBB(pos.add(-10, -5, -10), pos.add(10, 5, 10));

        for (double x = box.minX ; x <= box.maxX ; x++) {
            for (double y = box.minY ; y <= box.maxY ; y++) {
                for (double z = box.minZ ; z <= box.maxZ ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(p);
                    if (state.getBlock() == Blocks.FARMLAND) {
                        IBlockState cropState = world.getBlockState(p.up());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(p), world, p, EnumFacing.UP, (IPlantable) cropBlock);
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
        return new HarvestActionWorker(helper);
    }
}
