package mcjty.meecreeps.items;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {

    @GameRegistry.ObjectHolder("meecreeps:creepcube")
    public static CreepCubeItem creepCubeItem;

    @GameRegistry.ObjectHolder("meecreeps:portalgun")
    public static PortalGunItem portalGunItem;

    @GameRegistry.ObjectHolder("meecreeps:projectile")
    public static ProjectileItem projectileItem;


    @SideOnly(Side.CLIENT)
    public static void initModels() {
        creepCubeItem.initModel();
        portalGunItem.initModel();
        projectileItem.initModel();
    }

}
