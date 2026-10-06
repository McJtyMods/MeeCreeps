package mcjty.meecreeps.items;

import mcjty.meecreeps.config.ConfigSetup;
import net.minecraft.world.item.ItemStack;


/** Energy lives in stack data components so recipes and inventory sync preserve it. */
public final class ItemEnergy {
    private final ItemStack stack;
    public static final int ENERGY_PER_CHARGE = 1000;

    public ItemEnergy(ItemStack stack) {
        this.stack = stack;
    }

    public static int stored(ItemStack stack) {
        if (!StackData.has(stack))
            return 0;
        return Math.max(0, StackData.get(stack).getIntOr("charge", 0) * ENERGY_PER_CHARGE + StackData.get(stack).getIntOr("energyRemainder", 0));
    }

    public static void setCharge(ItemStack stack, int charge) {
        StackData.update(stack, data -> data.putInt("charge", Math.max(0, Math.min(charge, ConfigSetup.maxCharge.get()))));
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.max(0, Math.min(maxReceive, getMaxEnergyStored() - getEnergyStored()));
        if (!simulate && accepted > 0) {
            int energy = getEnergyStored() + accepted;
            StackData.update(stack, data -> {
                data.putInt("charge", energy / ENERGY_PER_CHARGE);
                data.putInt("energyRemainder", energy % ENERGY_PER_CHARGE);
            });
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

}
