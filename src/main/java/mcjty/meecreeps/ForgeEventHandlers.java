package mcjty.meecreeps;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> registry) {
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "teleport")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "teleport")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "portal")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "portal")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro1")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro1")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "ok")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "ok")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "intro2")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "intro2")));
        registry.getRegistry().register(new SoundEvent(new ResourceLocation(MeeCreeps.MODID, "ok2")).setRegistryName(new ResourceLocation(MeeCreeps.MODID, "ok2")));
    }


    @SubscribeEvent
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        ServerActionManager.getManager().tick();
    }

    public static final Map<BlockPos, Integer> harvestableBlocksToCollect = new HashMap<>();

    @SubscribeEvent
    public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent event) {
        BlockPos pos = event.getPos();
        if (harvestableBlocksToCollect.containsKey(pos)) {
            Integer actionId = harvestableBlocksToCollect.get(pos);
            ActionOptions options = ServerActionManager.getManager().getOptions(actionId);
            if (options != null) {
                options.registerDrops(pos, event.getDrops());
                event.getDrops().clear();
                ServerActionManager.getManager().save();
            } else {
                harvestableBlocksToCollect.remove(pos);
            }
        }
    }
}
