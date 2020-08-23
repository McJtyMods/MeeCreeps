package mcjty.meecreeps.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class HeldCubeBlock extends Block {

    public HeldCubeBlock() {
        super(Block.Properties.create(Material.IRON));
//        setUnlocalizedName(MeeCreeps.MODID + ".held_creepcube");
//        setRegistryName("creepcube");
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }
}
