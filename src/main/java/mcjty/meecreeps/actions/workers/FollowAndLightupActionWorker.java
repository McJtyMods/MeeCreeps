package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.teleport.TeleportationTools;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;

public class FollowAndLightupActionWorker extends AbstractActionWorker {

    public FollowAndLightupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AxisAlignedBB getActionBox() {
        return null;
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    private BlockPos findDarkSpot() {
        World world = entity.getWorld();
        BlockPos position = options.getPlayer().getPosition();
        AxisAlignedBB box = new AxisAlignedBB(position.add(-6, -4, -6), position.add(6, 4, 6));
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
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) this.entity;
        EntityPlayer player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (!entity.hasItem(this::isTorch)) {
            if (!helper.findItemOnGroundOrInChest(this::isTorch, "I cannot find any torches", Integer.MAX_VALUE)) {
                helper.taskIsDone();
            }
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                helper.navigateTo(darkSpot, this::placeTorch);
            } else if (player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension()) {
                // Wrong dimension. Teleport to the player
                BlockPos p = helper.findSuitablePositionNearPlayer(3.0);
                TeleportationTools.teleportEntity(meeCreep, player.getEntityWorld(), p.getX(), p.getY(), p.getZ(), EnumFacing.NORTH);
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(3.0);
                helper.navigateTo(p, blockPos -> {});
            }
        }
    }
}
