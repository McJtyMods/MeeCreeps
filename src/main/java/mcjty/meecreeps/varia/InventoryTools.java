package mcjty.meecreeps.varia;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class InventoryTools {

    public static boolean isInventory(Level world, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        BlockEntity te = world.getBlockEntity(pos);
        return (te != null && te.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent());
    }
}
