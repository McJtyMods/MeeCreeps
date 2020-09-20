package mcjty.meecreeps;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ForgeEventHandlers {

//    @SubscribeEvent
//    public void onWorldTick(TickEvent.WorldTickEvent event) {
//        // todo: make sure this is right
//        ServerActionManager.getManager(event.world).tick();
//    }

    public static final Map<BlockPos, Integer> harvestableBlocksToCollect = new HashMap<>();

    @SubscribeEvent
    public void onHarvestDropsEvent(BlockEvent.HarvestDropsEvent event) {
        BlockPos pos = event.getPos();
        if (harvestableBlocksToCollect.containsKey(pos)) {
            Integer actionId = harvestableBlocksToCollect.get(pos);
            ActionOptions options = ServerActionManager.getManager((World) event.getWorld()).getOptions(actionId);
            if (options != null) {
                options.registerDrops(pos, event.getDrops());
                event.getDrops().clear();
                ServerActionManager.getManager((World) event.getWorld()).save();
            } else {
                harvestableBlocksToCollect.remove(pos);
            }
        }
    }
}
