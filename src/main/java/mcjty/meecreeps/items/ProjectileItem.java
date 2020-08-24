package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.item.Item;

public class ProjectileItem extends Item {
    public ProjectileItem() {
        super(new Properties().group(MeeCreeps.setup.getTab()).maxStackSize(1));
//        setRegistryName("projectile");
//        setUnlocalizedName(MeeCreeps.MODID + ".projectile");
//        setMaxStackSize(1);
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }
}
