package mcjty.meecreeps.compat;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Function;

public class TopCompatibility implements Function<ITheOneProbe, Void> {
    public Void apply(ITheOneProbe probe) {
        probe.registerEntityProvider(new IProbeInfoEntityProvider() {
            public String getID() {
                return "meecreeps:task";
            }

            public void addProbeEntityInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, Entity entity, IProbeHitEntityData data) {
                if (world.isClientSide || !(entity instanceof EntityMeeCreeps creep))
                    return;
                var options = ServerActionManager.getManager().getOptions(creep.getActionId());
                if (options != null && options.getTask() != null) {
                    var factory = MeeCreeps.api.getFactory(options.getTask());
                    if (factory != null)
                        info.text(Component.translatable(factory.getMessage()));
                    if (options.isPaused())
                        info.text(Component.translatable("message.meecreeps.paused"));
                }
                if (mode == ProbeMode.EXTENDED)
                    for (var stack : creep.getInventory())
                        if (!stack.isEmpty())
                            info.item(stack);
            }
        });
        return null;
    }
}
