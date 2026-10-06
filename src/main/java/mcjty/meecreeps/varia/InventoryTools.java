package mcjty.meecreeps.varia;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

public class InventoryTools {

    public static net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> getHandler(Level world, BlockPos pos) {
        var handler = world.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP);
        return handler;
    }

    public static net.minecraft.world.item.ItemStack extract(
            net.neoforged.neoforge.transfer.ResourceHandler<net.neoforged.neoforge.transfer.item.ItemResource> handler,
            int slot, int amount) {
        var resource = handler.getResource(slot);
        if (resource.isEmpty() || amount <= 0) return net.minecraft.world.item.ItemStack.EMPTY;
        try (var transaction = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            int extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), transaction);
            transaction.commit();
            return resource.toStack(extracted);
        }
    }

    public static boolean isInventory(Level world, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        BlockEntity te = world.getBlockEntity(pos);
        return (te != null && world.getCapability(Capabilities.Item.BLOCK, pos, Direction.UP) != null);
    }
}
