package mcjty.meecreeps.items;

import mcjty.meecreeps.config.ConfigSetup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

/** Energy lives in stack NBT so recipes and inventory sync preserve it. */
public final class ItemEnergy implements ICapabilityProvider, IEnergyStorage {
    private final ItemStack stack;
    private final LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> this);
    public static final int ENERGY_PER_CHARGE = 1000;

    public ItemEnergy(ItemStack stack) {
        this.stack = stack;
    }

    public static int stored(ItemStack stack) {
        if (!stack.hasTag())
            return 0;
        return Math.max(0, stack.getTag().getInt("charge") * ENERGY_PER_CHARGE + stack.getTag().getInt("energyRemainder"));
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt("charge", Math.max(0, Math.min(charge, ConfigSetup.maxCharge.get())));
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.max(0, Math.min(maxReceive, getMaxEnergyStored() - getEnergyStored()));
        if (!simulate && accepted > 0) {
            int energy = getEnergyStored() + accepted;
            stack.getOrCreateTag().putInt("charge", energy / ENERGY_PER_CHARGE);
            stack.getOrCreateTag().putInt("energyRemainder", energy % ENERGY_PER_CHARGE);
        }
        return accepted;
    }

    public int extractEnergy(int amount, boolean simulate) {
        return 0;
    }

    public int getEnergyStored() {
        return Math.min(stored(stack), getMaxEnergyStored());
    }

    public int getMaxEnergyStored() {
        return ConfigSetup.maxCharge.get() * ENERGY_PER_CHARGE;
    }

    public boolean canExtract() {
        return false;
    }

    public boolean canReceive() {
        return true;
    }

    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == ForgeCapabilities.ENERGY ? capability.cast() : LazyOptional.empty();
    }
}
