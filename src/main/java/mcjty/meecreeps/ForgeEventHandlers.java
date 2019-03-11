package mcjty.meecreeps;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class ForgeEventHandlers {

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
