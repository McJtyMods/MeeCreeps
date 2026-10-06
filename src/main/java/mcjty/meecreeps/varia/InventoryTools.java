package mcjty.meecreeps.varia;

import net.fabricmc.fabric.api.transfer.v1.item.*;
import net.fabricmc.fabric.api.transfer.v1.storage.*;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class InventoryTools {
    /** A snapshot of the views; all mutations still go through Fabric transactions. */
    public record Inventory(Storage<ItemVariant> storage, List<StorageView<ItemVariant>> views) {
        public int size() { return views.size(); }
        public ItemVariant getResource(int slot) { return views.get(slot).getResource(); }
        public int getCapacityAsInt(int slot, ItemVariant resource) { return (int) Math.min(Integer.MAX_VALUE, views.get(slot).getCapacity()); }
    }
    public static Inventory getHandler(Level level, BlockPos pos) {
        var storage = ItemStorage.SIDED.find(level, pos, Direction.UP);
        if (storage == null) return null;
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        storage.forEach(views::add);
        return new Inventory(storage, views);
    }
    public static ItemStack getStack(Inventory inventory, int slot) {
        var view = inventory.views.get(slot);
        return view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
    }
    public static ItemStack extract(Inventory inventory, int slot, int amount) {
        var view = inventory.views.get(slot);
        var resource = view.getResource();
        if (resource.isBlank() || amount <= 0) return ItemStack.EMPTY;
        try (var tx = Transaction.openOuter()) {
            int extracted = (int) view.extract(resource, Math.min(amount, resource.toStack().getMaxStackSize()), tx);
            tx.commit();
            return resource.toStack(extracted);
        }
    }
    public static ItemStack insertItemReturnRemaining(Inventory inventory, ItemStack stack, boolean simulate, Object ignored) {
        return insert(inventory.storage, stack, simulate);
    }
    public static ItemStack insertItemReturnRemaining(Inventory inventory, int slot, ItemStack stack, boolean simulate, Object ignored) {
        if (inventory.storage instanceof SlottedStorage<ItemVariant> slots) return insert(slots.getSlot(slot), stack, simulate);
        return insert(inventory.storage, stack, simulate);
    }
    private static ItemStack insert(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try (var tx = Transaction.openOuter()) {
            int inserted = (int) storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
            if (!simulate) tx.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }
    public static boolean isInventory(Level world, BlockPos pos) {
        return pos != null && getHandler(world, pos) != null;
    }
}
