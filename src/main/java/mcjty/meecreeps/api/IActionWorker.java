package mcjty.meecreeps.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;

public interface IActionWorker {

    void tick(boolean timeToWrapUp);

    AxisAlignedBB getActionBox();

    /**
     * Return a sorted array of prefered chest locations for putting back items. The
     * first chest in this array will be tried first
     */
    PreferedChest[] getPreferedChests();

    default void readFromNBT(NBTTagCompound tag) {}

    default void writeToNBT(NBTTagCompound tag) {}
}
