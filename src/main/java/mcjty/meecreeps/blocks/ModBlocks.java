package mcjty.meecreeps.blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

    @GameRegistry.ObjectHolder("meecreeps:portalblock")
    public static PortalBlock portalBlock;

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        portalBlock.initModel();
    }

}
