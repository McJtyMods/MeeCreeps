package mcjty.meecreeps.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.meecreeps.MeeCreeps;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;

public class CommandTestApi implements Command<CommandSource> {
    private static final CommandListActions COMMAND = new CommandListActions();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("creepTest")
                .requires(commandSource -> commandSource.hasPermissionLevel(2))
                .executes(COMMAND);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();

        MeeCreeps.api.spawnMeeCreep("meecreeps.dig_down", null, player.getEntityWorld(), player.getPosition().down(),
                Direction.UP, null, true);

        return 0;
    }
}
