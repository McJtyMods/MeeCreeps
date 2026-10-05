package mcjty.meecreeps;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import java.util.*;

public class ForgeEventHandlers {
    private record HarvestKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record Tracked(int action, long expires) {
    }

    private static final Map<HarvestKey, Tracked> harvests = new HashMap<>();

    public static void trackHarvest(Level world, BlockPos pos, int action) {
        harvests.put(new HarvestKey(world.dimension(), pos.immutable()), new Tracked(action, world.getGameTime() + 1200));
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            ServerActionManager.getManager().tick();
            harvests.entrySet().removeIf(entry -> ServerActionManager.getManager().getOptions(entry.getValue().action) == null);
        }
    }

    @SubscribeEvent
    public void drop(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide || !(e.getEntity() instanceof ItemEntity item))
            return;
        var tracked = harvests.get(new HarvestKey(e.getLevel().dimension(), item.blockPosition()));
        if (tracked == null || tracked.expires < e.getLevel().getGameTime())
            return;
        var options = ServerActionManager.getManager().getOptions(tracked.action);
        if (options != null) {
            options.registerDrops(item.blockPosition(), List.of(item.getItem()));
            ServerActionManager.getManager().save();
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void stopped(ServerStoppedEvent e) {
        harvests.clear();
    }
}
