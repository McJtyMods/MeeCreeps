package mcjty.meecreeps;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import java.util.*;

public class FabricEventHandlers {
    public static void register() {
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player.isSpectator()) return net.minecraft.world.InteractionResult.PASS;
            var stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof mcjty.meecreeps.items.FirstUseItem item) {
                return item.onItemUseFirst(stack, new net.minecraft.world.item.context.UseOnContext(player, hand, hit));
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(mcjty.meecreeps.varia.LevelTools::setServer);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            stopped();
            mcjty.meecreeps.varia.ChestAnimation.stopped();
            mcjty.meecreeps.varia.LevelTools.setServer(null);
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> tick());
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_LEVEL_TICK.register(mcjty.meecreeps.varia.ChestAnimation::tick);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register(FabricEventHandlers::drop);
    }

    private record HarvestKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    private record Tracked(int action, long expires) {
    }

    private static final Map<HarvestKey, Tracked> harvests = new HashMap<>();

    public static void trackHarvest(Level world, BlockPos pos, int action) {
        harvests.put(new HarvestKey(world.dimension(), pos.immutable()), new Tracked(action, world.getGameTime() + 1200));
    }

    public static void tick() {
        ServerActionManager.getManager().tick();
        harvests.entrySet().removeIf(entry -> ServerActionManager.getManager().getOptions(entry.getValue().action) == null);
    }

    public static void drop(net.minecraft.world.entity.Entity entity, net.minecraft.server.level.ServerLevel level) {
        if (level.isClientSide() || !(entity instanceof ItemEntity item))
            return;
        var tracked = harvests.get(new HarvestKey(level.dimension(), item.blockPosition()));
        if (tracked == null || tracked.expires < level.getGameTime())
            return;
        var options = ServerActionManager.getManager().getOptions(tracked.action);
        if (options != null) {
            options.registerDrops(item.blockPosition(), List.of(item.getItem()));
            ServerActionManager.getManager().save();
            item.discard();
        }
    }

    public static void stopped() {
        harvests.clear();
    }
}
