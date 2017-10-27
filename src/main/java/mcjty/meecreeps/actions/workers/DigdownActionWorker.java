package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DigdownActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    @Override
    protected AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -5, -10), options.getPos().add(10, 5, 10));
        }
        return actionBox;
    }


    public DigdownActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        super(entity, options);
    }

    private boolean isLadder(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.LADDER);
    }

    private void placeLadder(BlockPos pos) {
        World world = entity.getEntityWorld();
        ItemStack ladder = entity.consumeItem(this::isLadder, 1);
        if (!ladder.isEmpty()) {
            entity.getEntityWorld().setBlockState(pos, Blocks.LADDER.getDefaultState(), 3);
            SoundTools.playSound(world, Blocks.LADDER.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        }
    }

    private BlockPos findTopSpotNotDiggedYet() {
        BlockPos p = options.getPos();
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(p);
        while (world.isAirBlock(p) || state.getBlock() == Blocks.LADDER) {
            p = p.down();
            state = world.getBlockState(p);
        }
        return p;
    }

    private void digDown() {
        World world = entity.getEntityWorld();
        BlockPos p = findTopSpotNotDiggedYet();
        IBlockState state = world.getBlockState(p);
        if (state.getBlock().getBlockHardness(state, world, p) >= 0 && allowedToHarvest(state, world, p, GeneralTools.getHarvester())) {
            harvestAndDrop(p);
            placeLadder(p);
        } else {
            // Too hard or not allowed. We stop here
            taskIsDone();
        }
    }

    @Override
    protected void performTick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            done();
        } else if (!entity.hasItem(this::isLadder)) {
            findItemOnGroundOrInChest(this::isLadder, "I cannot find any ladders");
        } else {
            BlockPos p = findTopSpotNotDiggedYet();
            navigateTo(p, blockPos -> digDown());
        }
    }
}
