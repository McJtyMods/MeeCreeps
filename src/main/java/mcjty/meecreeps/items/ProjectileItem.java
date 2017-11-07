package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ProjectileItem extends Item {

    public ProjectileItem() {
        setRegistryName("projectile");
        setUnlocalizedName(MeeCreeps.MODID + ".projectile");
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
