package mcjty.meecreeps.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

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
     * Optionally return a box on which this action should operate and where the IWorkerHelper can look for
     * stuff (like inventories and items on the ground).
     * @return
     */
    @Nullable
    AxisAlignedBB getActionBox();

    /**
     * Return a sorted array of prefered chest locations for putting back items. The
     * first chest in this array will be tried first
     */
    PreferedChest[] getPreferedChests();

    default void readFromNBT(NBTTagCompound tag) {}

    default void writeToNBT(NBTTagCompound tag) {}
}
