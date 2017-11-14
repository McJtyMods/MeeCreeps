package mcjty.meecreeps.blocks;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HeldCubeBlock extends Block {

    public HeldCubeBlock() {
        super(Material.IRON);
        setUnlocalizedName(MeeCreeps.MODID + ".held_creepcube");
        setRegistryName("creepcube");
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
