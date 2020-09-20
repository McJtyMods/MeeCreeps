package mcjty.meecreeps.setup;

import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.meecreeps.MeeCreeps.MODID;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
//    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
//    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MODID);

    public static void register(IEventBus bus) {
        ModBlocks.register();
        ModItems.register();
        ModEntities.register();

        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);
//        CONTAINERS.register(bus);
//        SOUNDS.register(bus);
        ENTITIES.register(bus);
    }
}
