package mcjty.meecreeps.varia;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;

public class InventoryTools {

    public static boolean isInventory(Level world, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        BlockEntity te = world.getBlockEntity(pos);
        return (te != null && world.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP) != null);
    }
}
