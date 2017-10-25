package mcjty.meecreeps.actions;

import net.minecraft.nbt.NBTTagCompound;

public interface IActionWorker {

    void tick(boolean lastTask);

    default void readFromNBT(NBTTagCompound tag) {}

    default void writeToNBT(NBTTagCompound tag) {}
}
