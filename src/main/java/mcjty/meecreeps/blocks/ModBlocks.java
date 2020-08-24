package mcjty.meecreeps.blocks;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.meecreeps.setup.Registration.BLOCKS;
import static mcjty.meecreeps.setup.Registration.TILES;

public class ModBlocks {

    public static void register() {}

    public static final RegistryObject<Block> PORTAL_BLOCK = BLOCKS.register("portalblock", PortalBlock::new);
    public static final RegistryObject<TileEntityType<?>> PORTAL_TILE_ENTITY = TILES.register("portaltileentity", () -> TileEntityType.Builder.create(PortalTileEntity::new, PORTAL_BLOCK.get()).build(null));

    public static final RegistryObject<Block> CREEP_CUBE = BLOCKS.register("creepcube", HeldCubeBlock::new);
//
//    public static void initModels() {
//        portalBlock.initModel();
//        heldCubeBlock.initModel();
//    }
}
