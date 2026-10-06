package mcjty.meecreeps.actions.factories;

import mcjty.meecreeps.actions.workers.HarvestActionWorker;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NonNull;

public class HarvestActionFactory implements IActionFactory {

    @Override
    public boolean isPossible(Level world, BlockPos pos, Direction side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(-10, -5, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(10, 5, 10)));

        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos p = BlockPos.containing(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if ((state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.SOUL_SAND)) {
                        BlockState cropState = world.getBlockState(p.above());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = (cropBlock instanceof CropBlock || cropBlock instanceof NetherWartBlock)
                                && cropState.canSurvive(world, p.above());
                        if (hasCrops) {
                            if (cropBlock instanceof CropBlock) {
                                CropBlock crops = (CropBlock) cropBlock;
                                int age = crops.getAge(cropState);
                                int maxAge = crops.getMaxAge();
                                if (age >= maxAge) {
                                    return true;
                                }
                            } else if (cropBlock instanceof NetherWartBlock) {
                                NetherWartBlock wart = (NetherWartBlock) cropBlock;
                                int age = cropState.getValue(NetherWartBlock.AGE);
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
    public boolean isPossibleSecondary(Level world, BlockPos pos, Direction side) {
        if (!InventoryTools.isInventory(world, pos)) {
            return false;
        }

        // @todo config for harvest area
        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(-10, -5, -10)), net.minecraft.world.phys.Vec3.atLowerCornerOf(pos.offset(10, 5, 10)));

        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos p = BlockPos.containing(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if ((state.getBlock() == Blocks.FARMLAND || state.getBlock() == Blocks.SOUL_SAND)) {
                        BlockState cropState = world.getBlockState(p.above());
                        Block cropBlock = cropState.getBlock();
                        boolean hasCrops = (cropBlock instanceof CropBlock || cropBlock instanceof NetherWartBlock)
                                && cropState.canSurvive(world, p.above());
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
    public IActionWorker createWorker(@NonNull IWorkerHelper helper) {
        return new HarvestActionWorker(helper);
    }
}
