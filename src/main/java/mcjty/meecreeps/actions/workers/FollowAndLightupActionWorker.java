package mcjty.meecreeps.actions.workers;

import net.minecraft.core.Direction;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class FollowAndLightupActionWorker extends AbstractActionWorker {

    public FollowAndLightupActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public AABB getActionBox() {
        return null;
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public boolean needsToFollowPlayer() {
        return true;
    }

    private BlockPos findDarkSpot() {
        Level world = helper.getMeeCreep().getWorld();
        BlockPos position = options.getPlayer().blockPosition();
        AABB box = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(-6, -4, -6)), net.minecraft.world.phys.Vec3.atLowerCornerOf(position.offset(6, 4, 6)));
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (world.isEmptyBlock(p) && world.getBlockState(p.below()).isFaceSturdy(world, p.below(), Direction.UP)) {
                // Ignore daylight so exposed areas are also prepared for night.
                int light = world.getBrightness(LightLayer.BLOCK, p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        IMeeCreep entity = helper.getMeeCreep();
        Level world = entity.getWorld();
        int light = world.getBrightness(LightLayer.BLOCK, pos);
        if (light < 7 && world.isEmptyBlock(pos)
                && world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) {
            ItemStack torch = entity.consumeItem(WorkerHelper::isTorch, 1);
            if (!torch.isEmpty()) {
                helper.placeStackAt(torch, world, pos);
                entity.addStack(torch);
            }
        }
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        IMeeCreep entity = helper.getMeeCreep();
        EntityMeeCreeps meeCreep = (EntityMeeCreeps) entity;
        Player player = options.getPlayer();

        if (timeToWrapUp) {
            helper.done();
        } else if (player == null) {
            helper.taskIsDone();
        } else if (!entity.hasItem(WorkerHelper::isTorch)) {
            if (!helper.findItemOnGroundOrInChest(WorkerHelper::isTorch, Integer.MAX_VALUE, "message.meecreeps.cant_find_torches")) {
                helper.taskIsDone();
            }
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                helper.navigateTo(darkSpot, this::placeTorch);
            } else if (player.level().dimension() != meeCreep.level().dimension()) {
                // Wrong dimension, do nothing as this is handled by ServerActionManager
            } else {
                // Find a spot close to the player where we can navigate too
                BlockPos p = helper.findSuitablePositionNearPlayer(4.0);
                helper.navigateTo(p, blockPos -> {
                });
            }
        }
    }
}
