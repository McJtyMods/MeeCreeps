package mcjty.meecreeps.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A helper that you can use for your actions.
 */
public interface IWorkerHelper {

    /**
     * Return the current context
     */
    IActionContext getContext();

    /**
     * Return the MeeCreep entity
     */
    IMeeCreep getMeeCreep();

    /**
     * Return true if it is legal to harvest this block
     */
    boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer);

    /**
     * Harvest the given block and give the drops to the MeeCreep. If there is no room the
     * MeeCreep will drop the remaining items and try to fetch them later
     */
    void harvestAndPickup(BlockPos pos);

    /**
     * Harvest the given block and drop the drops
     */
    void harvestAndDrop(BlockPos pos);

    /**
     * Pick up as much of the entity item as possible. If everything was picked up the entity item
     * will be marked dead. Otherwise the MeeCreep will try to get the rest later
     */
    void pickup(EntityItem item);

    /**
     * It is time to stop. This will completely stop the MeeCreep from working. He will not be given
     * a chance to finish current jobs or put back items in the chest. Items he is still holding
     * will drop on the ground.
     */
    void done();

    /**
     * Indicate the task is done and that it is time to do the last task (putting back stuff etc)
     */
    void taskIsDone();

    /**
     * Indicate that it is time to put stuff away before proceeding with other tasks
     */
    void putStuffAway();

    // Speed up stuff
    void speedUp(int t);

    /**
     * Give the list of items to the meecreeps. If the meecreeps cannot hold them they are
     * dropped and the meecreeps will try to fetch them later
     */
    void giveDropsToMeeCreeps(@Nonnull List<ItemStack> drops);

    /**
     * Register a block for harvesting. When this block is later harvested (by the MeeCreep or during
     * some other way, like leave decay) the drops will go to the MeeCreep inventory if there is room. The
     * other items are dropped on the ground as usual
     */
    void registerHarvestableBlock(BlockPos pos);

    /**
     * Navigate to the given position and as soon as the MeeCreep is close enough to that position
     * perform the given job. If navigation is not possible the MeeCreep will teleport. While the
     * MeeCreep is moving to the position no ticks will be performed on this action
     */
    void navigateTo(BlockPos pos, Consumer<BlockPos> job);

    /**
     * Navigate to the given entity. If the entity is dead or too far this will return false and
     * nothing happens (the job is not executed). Otherwise the job will be executed as soon as
     * the MeeCreep arrives at the destination.
     * If the entity dies during the navigation then the MeeCreep will stop moving towards it
     * and normal operations resume
     */
    boolean navigateTo(Entity dest, Consumer<BlockPos> job, double maxDist);

    /**
     * Navigate to the given entity. If the entity is dead this will return false and
     * nothing happens (the job is not executed). Otherwise the job will be executed as soon as
     * the MeeCreep arrives at the destination
     * If the entity dies during the navigation then the MeeCreep will stop moving towards it
     * and normal operations resume
     */
    boolean navigateTo(Entity dest, Consumer<BlockPos> job);

    /**
     * Drop an item. Mark it for later retreival and then proceed to put away the items in inventory
     */
    void dropAndPutAwayLater(ItemStack stack);

    /**
     * If the player is present and not too far this will setup navigation towards the player in order
     * to give the inventory to the player. If this fails the MeeCreep will simply drop the items
     */
    void giveToPlayerOrDrop();

    /**
     * Find items matching the predicate on the ground or else in a nearby chest (uses getActionBox()). Shows
     * a message to the player if no such items can be found (the player can then drop those items on the
     * ground or put them in a nearby chest)
     */
    void findItemOnGroundOrInChest(Predicate<ItemStack> matcher, String message);

    /**
     * Find an item that matches the predicate. If there is such an item then navigate to it and
     * finally execute the job. Otherwise return false.
     */
    boolean findItemOnGround(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<EntityItem> job);

    /**
     * Put the entire inventory in a given chest and drop everything else that didn't fit. Note that
     * this does not do any navigation! The position is assumed to be reachable
     */
    void putInventoryInChest(BlockPos pos);

    /**
     * Find an inventory in the given box that contains items matching the predicate. Navigate to that
     * inventory and execute the job. If no such inventory could be found return false
     */
    boolean findSuitableInventory(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job);
}
