package mcjty.meecreeps.setup;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.blocks.*;
import mcjty.meecreeps.items.*;
import mcjty.meecreeps.entities.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;


public final class Registration {
    public static final Supplier<Block> CUBE = register(BuiltInRegistries.BLOCK, "creepcube", HeldCubeBlock::new);
    public static final Supplier<Block> PORTAL = register(BuiltInRegistries.BLOCK, "portalblock", PortalBlock::new);
    public static final Supplier<Item> CUBE_ITEM = register(BuiltInRegistries.ITEM, "creepcube", CreepCubeItem::new);
    public static final Supplier<Item> GUN = register(BuiltInRegistries.ITEM, "portalgun", PortalGunItem::new);
    public static final Supplier<Item> EMPTY_GUN = register(BuiltInRegistries.ITEM, "emptyportalgun", EmptyPortalGunItem::new);
    public static final Supplier<Item> CARTRIDGE = register(BuiltInRegistries.ITEM, "cartridge", CartridgeItem::new);
    public static final Supplier<Item> PROJECTILE_ITEM = register(BuiltInRegistries.ITEM, "projectile", mcjty.meecreeps.items.ProjectileItem::new);
    public static final Supplier<Item> PORTAL_ITEM = register(BuiltInRegistries.ITEM, "portalblock", () -> new BlockItem(PORTAL.get(), new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "portalblock")))));
    public static final Supplier<EntityType<EntityMeeCreeps>> CREEP = register(BuiltInRegistries.ENTITY_TYPE, "meecreeps", () -> EntityType.Builder.<EntityMeeCreeps>of(EntityMeeCreeps::new, MobCategory.CREATURE).sized(.6F, 1.95F).clientTrackingRange(10).build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("meecreeps:meecreeps"))));
    public static final Supplier<EntityType<EntityProjectile>> PROJECTILE = register(BuiltInRegistries.ENTITY_TYPE, "projectile", () -> EntityType.Builder.<EntityProjectile>of(EntityProjectile::new, MobCategory.MISC).sized(.25F, .25F).clientTrackingRange(8).updateInterval(1).build(net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("meecreeps:projectile"))));
    public static final Supplier<BlockEntityType<PortalTileEntity>> PORTAL_TILE = register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "portalblock", () -> net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(PortalTileEntity::new, PORTAL.get()).build());
    public static final Supplier<RecipeSerializer<InsertCartridgeFactory>> INSERT = register(BuiltInRegistries.RECIPE_SERIALIZER, "insert_cartridge_factory", () -> new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(InsertCartridgeFactory::new), net.minecraft.network.codec.StreamCodec.unit(new InsertCartridgeFactory())));
    public static final Supplier<RecipeSerializer<RemoveCartridgeFactory>> REMOVE = register(BuiltInRegistries.RECIPE_SERIALIZER, "remove_cartridge_factory", () -> new RecipeSerializer<>(com.mojang.serialization.MapCodec.unit(RemoveCartridgeFactory::new), net.minecraft.network.codec.StreamCodec.unit(new RemoveCartridgeFactory())));

    static {
        for (String sound : new String[]{"teleport", "portal", "intro1", "intro2", "intro3", "intro4", "ok", "ok2"})
            register(BuiltInRegistries.SOUND_EVENT, sound, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MeeCreeps.MODID, sound)));
        register(BuiltInRegistries.CREATIVE_MODE_TAB, "meecreeps", () -> net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder().title(Component.literal("MeeCreeps")).icon(() -> new ItemStack(GUN.get())).displayItems((p, o) -> {
            o.accept(CUBE_ITEM.get());
            o.accept(GUN.get());
            o.accept(EMPTY_GUN.get());
            o.accept(CARTRIDGE.get());
        }).build());
    }

    private static <T, V extends T> Supplier<V> register(Registry<T> registry, String name, Supplier<V> factory) {
        V value = Registry.register(registry, Identifier.fromNamespaceAndPath(MeeCreeps.MODID, name), factory.get());
        return () -> value;
    }

    public static void register() {
        net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(CREEP.get(), EntityMeeCreeps.createAttributes());
        team.reborn.energy.api.EnergyStorage.ITEM.registerForItems((stack, context) -> new ChargingItemEnergy(context), GUN.get(), CARTRIDGE.get());
    }
}
