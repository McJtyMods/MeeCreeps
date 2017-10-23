package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionFactory;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.workers.HarvestReplantActionWorker;
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

public class HarvestReplantActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(World world, BlockPos pos) {
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
    public boolean isPossibleSecondary(World world, BlockPos pos) {
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
    public IActionWorker createWorker(ActionOptions options) {
        return new HarvestReplantActionWorker(options);
    }
}