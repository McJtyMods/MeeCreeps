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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.registries.*;
import net.neoforged.bus.api.IEventBus;

public final class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MeeCreeps.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MeeCreeps.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MeeCreeps.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MeeCreeps.MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MeeCreeps.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MeeCreeps.MODID);
    public static final DeferredHolder<Block, Block> CUBE = BLOCKS.register("creepcube", HeldCubeBlock::new);
    public static final DeferredHolder<Block, Block> PORTAL = BLOCKS.register("portalblock", PortalBlock::new);
    public static final DeferredHolder<Item, Item> CUBE_ITEM = ITEMS.register("creepcube", CreepCubeItem::new);
    public static final DeferredHolder<Item, Item> GUN = ITEMS.register("portalgun", PortalGunItem::new);
    public static final DeferredHolder<Item, Item> EMPTY_GUN = ITEMS.register("emptyportalgun", EmptyPortalGunItem::new);
    public static final DeferredHolder<Item, Item> CARTRIDGE = ITEMS.register("cartridge", CartridgeItem::new);
    public static final DeferredHolder<Item, Item> PROJECTILE_ITEM = ITEMS.register("projectile", mcjty.meecreeps.items.ProjectileItem::new);
    public static final DeferredHolder<Item, Item> PORTAL_ITEM = ITEMS.register("portalblock", () -> new BlockItem(PORTAL.get(), new Item.Properties()));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityMeeCreeps>> CREEP = ENTITIES.register("meecreeps", () -> EntityType.Builder.<EntityMeeCreeps>of(EntityMeeCreeps::new, MobCategory.CREATURE).sized(.6F, 1.95F).clientTrackingRange(10).build("meecreeps:meecreeps"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntityProjectile>> PROJECTILE = ENTITIES.register("projectile", () -> EntityType.Builder.<EntityProjectile>of(EntityProjectile::new, MobCategory.MISC).sized(.25F, .25F).clientTrackingRange(8).updateInterval(1).build("meecreeps:projectile"));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PortalTileEntity>> PORTAL_TILE = TILES.register("portalblock", () -> BlockEntityType.Builder.of(PortalTileEntity::new, PORTAL.get()).build(null));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InsertCartridgeFactory>> INSERT = RECIPES.register("insert_cartridge_factory", () -> new SimpleCraftingRecipeSerializer<>(InsertCartridgeFactory::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RemoveCartridgeFactory>> REMOVE = RECIPES.register("remove_cartridge_factory", () -> new SimpleCraftingRecipeSerializer<>(RemoveCartridgeFactory::new));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MeeCreeps.MODID);

    static {
        for (String sound : new String[]{"teleport", "portal", "intro1", "intro2", "intro3", "intro4", "ok", "ok2"})
            SOUNDS.register(sound, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MeeCreeps.MODID, sound)));
        TABS.register("meecreeps", () -> CreativeModeTab.builder().title(Component.literal("MeeCreeps")).icon(() -> new ItemStack(GUN.get())).displayItems((p, o) -> {
            o.accept(CUBE_ITEM.get());
            o.accept(GUN.get());
            o.accept(EMPTY_GUN.get());
            o.accept(CARTRIDGE.get());
        }).build());
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        TILES.register(bus);
        SOUNDS.register(bus);
        RECIPES.register(bus);
        TABS.register(bus);
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e) ->
                e.registerItem(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM,
                        (stack, context) -> new ItemEnergy(stack), GUN.get(), CARTRIDGE.get()));
        bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e) -> e.put(CREEP.get(), EntityMeeCreeps.createAttributes().build()));
    }
}
