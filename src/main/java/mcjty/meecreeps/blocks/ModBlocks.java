package mcjty.meecreeps.blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {

    @ObjectHolder("meecreeps:portalblock")
    public static PortalBlock portalBlock;

    @ObjectHolder("meecreeps:creepcube")
    public static HeldCubeBlock heldCubeBlock;

    public static void initModels() {
        portalBlock.initModel();
        heldCubeBlock.initModel();
    }
}
