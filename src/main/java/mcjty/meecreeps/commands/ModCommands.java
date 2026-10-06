package mcjty.meecreeps.commands;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class ModCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("meecreeps")
                .then(Commands.literal("list").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
                    ServerActionManager.getManager().listOptions(c.getSource());
                    return 1;
                }))
                .then(Commands.literal("clear").executes(c -> {
                            ServerActionManager.getManager().clearOptions(c.getSource(), c.getSource().getPlayerOrException());
                            return 1;
                        })
                        .then(Commands.literal("all").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
                            ServerActionManager.getManager().clearOptions(c.getSource(), null);
                            return 1;
                        }))));
        event.getDispatcher().register(Commands.literal("creep_list").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            ServerActionManager.getManager().listOptions(c.getSource());
            return 1;
        }));
        event.getDispatcher().register(Commands.literal("creep_clear").executes(c -> {
            var source = c.getSource();
            var player = source.getPlayer();
            boolean clearAll = source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER) && (player == null || player.isCreative());
            if (player == null && !clearAll)
                return 0;
            ServerActionManager.getManager().clearOptions(source, clearAll ? null : player);
            return 1;
        }));
        event.getDispatcher().register(Commands.literal("creep_test").requires(s -> s.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)).executes(c -> {
            var player = c.getSource().getPlayerOrException();
            if (!player.isCreative())
                return 0;
            return mcjty.meecreeps.MeeCreeps.api.spawnMeeCreep("meecreeps.dig_down", null, player.level(), player.blockPosition().below(), net.minecraft.core.Direction.UP, null, true) ? 1 : 0;
        }));
    }
}
