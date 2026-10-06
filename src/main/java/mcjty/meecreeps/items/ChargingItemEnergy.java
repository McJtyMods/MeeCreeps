package mcjty.meecreeps.items;

import mcjty.meecreeps.config.ConfigSetup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Charges the item through its inventory location, with transactional rollback. */
public final class ChargingItemEnergy implements EnergyHandler {
    private final ItemAccess access;
    private final Item item;
    public ChargingItemEnergy(ItemAccess access) {
        this.access = access;
        item = access.getResource().getItem();
    }
    @Override public long getAmountAsLong() {
        return access.getResource().is(item) ? ItemEnergy.stored(access.getResource().toStack()) : 0;
    }
    @Override public long getCapacityAsLong() {
        return access.getResource().is(item) ? (long) ConfigSetup.maxCharge.get() * ItemEnergy.ENERGY_PER_CHARGE : 0;
    }
    @Override public int insert(int amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative energy amount");
        if (access.getAmount() != 1 || !access.getResource().is(item)) return 0;
        int accepted = (int) Math.min(amount, Math.max(0, getCapacityAsLong() - getAmountAsLong()));
        if (accepted == 0) return 0;
        int energy = (int) getAmountAsLong() + accepted;
        var tag = access.getResource().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("charge", energy / ItemEnergy.ENERGY_PER_CHARGE);
        tag.putInt("energyRemainder", energy % ItemEnergy.ENERGY_PER_CHARGE);
        var updated = access.getResource().with(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return access.exchange(updated, 1, transaction) == 1 ? accepted : 0;
    }
    @Override public int extract(int amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("Negative energy amount");
        return 0;
    }
}
