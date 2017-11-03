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

public interface IWorkerHelper {

    IActionOptions getActionOptions();

    IMeeCreep getMeeCreeps();

    void harvestAndPickup(BlockPos pos);

    void harvestAndDrop(BlockPos pos);

    void pickup(EntityItem item);

    boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer);

    void done();

    // Indicate the task is done and that it is time to do the last task (putting back stuff etc)
    void taskIsDone();

    // Indicate that it is time to put stuff away
    void putStuffAway();

    // Speed up stuff
    void speedUp(int t);

    /**
     * Give the list of items to the meecreeps. If the meecreeps cannot hold them they are
     * dropped and the meecreeps will try to fetch them later
     */
    void giveDropsToMeeCreeps(@Nonnull List<ItemStack> drops);

    void navigateTo(BlockPos pos, Consumer<BlockPos> job);

    boolean navigateTo(Entity dest, Consumer<BlockPos> job, double maxDist);

    void navigateTo(Entity dest, Consumer<BlockPos> job);

    void dropAndPutAwayLater(ItemStack stack);

    void giveToPlayerOrDrop();

    void findItemOnGroundOrInChest(Predicate<ItemStack> matcher, String message);

    boolean findItemOnGround(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<EntityItem> job);

    void putInventoryInChest(BlockPos pos);

    boolean findSuitableInventory(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job);

    List<BlockPos> findInventoriesWithMostSpace(AxisAlignedBB box);

    boolean tryFindingItemsToPickup();
}
