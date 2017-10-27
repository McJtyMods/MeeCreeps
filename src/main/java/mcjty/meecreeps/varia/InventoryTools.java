package mcjty.meecreeps.varia;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

public class InventoryTools {

    public static boolean isInventory(World world, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        TileEntity te = world.getTileEntity(pos);
        return (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP));
    }
}
