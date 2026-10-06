package mcjty.meecreeps.items;

import mcjty.meecreeps.config.ConfigSetup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import team.reborn.energy.api.EnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/** Charges the item through its inventory location, with transactional rollback. */
public final class ChargingItemEnergy implements EnergyStorage {
    private final ContainerItemContext access;
    private final Item item;
    public ChargingItemEnergy(ContainerItemContext access) {
        this.access = access;
        item = access.getItemVariant().getItem();
    }
    @Override public long getAmount() {
        return access.getItemVariant().isOf(item) ? ItemEnergy.stored(access.getItemVariant().toStack()) : 0;
    }
    @Override public long getCapacity() {
        return access.getItemVariant().isOf(item) ? (long) ConfigSetup.maxCharge.get() * ItemEnergy.ENERGY_PER_CHARGE : 0;
    }
    @Override public long insert(long amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative energy amount");
        if (!access.getItemVariant().isOf(item) || access.getAmount() != 1) return 0;
        int accepted = (int) Math.min(amount, Math.max(0, getCapacity() - getAmount()));
        if (accepted == 0) return 0;
        int energy = (int) getAmount() + accepted;
        var tag = access.getItemVariant().toStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("charge", energy / ItemEnergy.ENERGY_PER_CHARGE);
        tag.putInt("energyRemainder", energy % ItemEnergy.ENERGY_PER_CHARGE);
        var stack = access.getItemVariant().toStack();
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        var updated = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack);
        return access.exchange(updated, 1, transaction) == 1 ? accepted : 0;
    }
    @Override public long extract(long amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative energy amount");
        return 0;
    }
}
