package mcjty.meecreeps.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.meecreeps.setup.Registration.ITEMS;

public class ModItems {

    public static void register() {}

    public static final RegistryObject<Item> CREEP_CUBE_ITEM = ITEMS.register("creepcube", CreepCubeItem::new);
    public static final RegistryObject<Item> PORTAL_GUN_ITEM = ITEMS.register("portalgun", PortalGunItem::new);
    public static final RegistryObject<Item> EMPTY_PORTAL_GUN_ITEM = ITEMS.register("emptyportalgun", EmptyPortalGunItem::new);
    public static final RegistryObject<Item> PROJECTILE_ITEM = ITEMS.register("projectile", CreepCubeItem::new);
    public static final RegistryObject<Item> CARTRIDGE_ITEM = ITEMS.register("cartridge", CreepCubeItem::new);

//    @SideOnly(Side.CLIENT)
//    public static void initModels() {
//        creepCubeItem.initModel();
//        portalGunItem.initModel();
//        emptyPortalGunItem.initModel();
//        projectileItem.initModel();
//        cartridgeItem.initModel();
//    }
}
