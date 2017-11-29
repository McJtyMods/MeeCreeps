package mcjty.meecreeps.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This task does the actual work
 */
public interface IActionWorker {

    /**
     * This is called every tick unless the MeeCreep is busy doing other things (like putting away items
     * or navigating to a location)
     * @param timeToWrapUp if true then it is time to wrap up and you should only finish up what you're doing
     */
    void tick(boolean timeToWrapUp);

    /**
     * Called at initialization time. In contrast with the constructor the helper will be correctly setup here
     * @param meeCreep
     */
    default void init(IMeeCreep meeCreep) { }

    /**
     * Optionally return a box on which this action should operate. This is optional in case the action does
     * not require such a box
     */
    @Nullable
    AxisAlignedBB getActionBox();

    /**
     * Return a box where the IWorkerHelper can look for stuff (like inventories and items on the ground).
     * This must be provided.
     */
    @Nonnull
    AxisAlignedBB getSearchBox();

    /**
     * If this returns true the MeeCreep will only finish when the task is done. Not earlier
     */
    default boolean onlyStopWhenDone() { return false; }

    /**
     * If this returns true the MeeCreep wants to follow the player. Returning true here ensures
     * that the MeeCreep will follow the player even when the player suddenly teleports very far
     * or to another dimension. If this flag is not set the MeeCreep may become unloaded and
     * fail to teleport by itself. Note that this long-distance teleport will cancel any current job
     */
    default boolean needsToFollowPlayer() { return false; }

    /**
     * Return a sorted array of prefered chest locations for putting back items. The
     * first chest in this array will be tried first
     */
    PreferedChest[] getPreferedChests();

    default void readFromNBT(NBTTagCompound tag) {}

    default void writeToNBT(NBTTagCompound tag) {}
}
