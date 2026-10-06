package mcjty.meecreeps.items;

import net.minecraft.world.item.Item;

public class ProjectileItem extends Item {
    public ProjectileItem() {
        super(new Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "projectile"))));
    }
}
