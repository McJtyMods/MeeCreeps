package mcjty.meecreeps.api;

import net.minecraft.nbt.NBTTagCompound;

public interface IActionWorker {

    void tick(boolean timeToWrapUp);

    default void readFromNBT(NBTTagCompound tag) {}

    default void writeToNBT(NBTTagCompound tag) {}
}
