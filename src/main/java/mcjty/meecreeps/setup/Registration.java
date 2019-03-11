package mcjty.meecreeps.setup;


import mcjty.lib.datafix.fixes.TileEntityNamespace;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.blocks.HeldCubeBlock;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.blocks.PortalBlock;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.items.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModFixs modFixs = FMLCommonHandler.instance().getDataFixer().init(MeeCreeps.MODID, 1);
        Map<String, String> oldToNewIdMap = new HashMap<>();

        event.getRegistry().register(new HeldCubeBlock());
        event.getRegistry().register(new PortalBlock());
        GameRegistry.registerTileEntity(PortalTileEntity.class, MeeCreeps.MODID + ":portalblock");

        // We used to accidentally register TEs with names like "minecraft:meecreeps_portalblock" instead of "meecreeps:portalblock".
        // Set up a DataFixer to map these incorrect names to the correct ones, so that we don't break old saved games.
        // @todo Remove all this if we ever break saved-game compatibility.
        oldToNewIdMap.put(MeeCreeps.MODID + "_portalblock", MeeCreeps.MODID + ":portalblock");
        oldToNewIdMap.put("minecraft:" + MeeCreeps.MODID + "_portalblock", MeeCreeps.MODID + ":portalblock");
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityNamespace(oldToNewIdMap, 1));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new CreepCubeItem());
        event.getRegistry().register(new PortalGunItem());
        event.getRegistry().register(new EmptyPortalGunItem());
        event.getRegistry().register(new ProjectileItem());
        event.getRegistry().register(new CartridgeItem());
        event.getRegistry().register(new ItemBlock(ModBlocks.portalBlock).setRegistryName(ModBlocks.portalBlock.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> registry) {
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "teleport")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "teleport")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "portal")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "portal")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro1")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro1")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro2")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro2")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro3")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro3")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro4")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro4")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "ok")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "ok")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "ok2")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "ok2")));
    }
}
