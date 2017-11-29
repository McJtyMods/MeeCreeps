package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class LightupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;

    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-10, -5, -10), options.getTargetPos().add(10, 5, 10));
        }
        return actionBox;
    }


    public LightupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    private BlockPos findDarkSpot() {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        AxisAlignedBB box = getActionBox();
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (world.isAirBlock(p) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, p)) {
                int light = world.getLightFromNeighbors(p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        World world = entity.getWorld();
        int light = world.getLightFromNeighbors(pos);
        if (light < 7) {
            ItemStack torch = entity.consumeItem(this::isTorch, 1);
            if (!torch.isEmpty()) {
                entity.getWorld().setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
                SoundTools.playSound(world, Blocks.TORCH.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
            }
        }
    }

    private boolean isTorch(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.TORCH);
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        if (timeToWrapUp) {
            helper.done();
        } else if (!entity.hasItem(this::isTorch)) {
            helper.findItemOnGroundOrInChest(this::isTorch, "I cannot find any torches", 128);
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                helper.navigateTo(darkSpot, this::placeTorch);
            } else {
                helper.taskIsDone();
            }
        }
    }

}