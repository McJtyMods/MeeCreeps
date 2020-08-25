package mcjty.meecreeps.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandListActions implements Command<CommandSource> {

    private static final CommandListActions COMMAND = new CommandListActions();

    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("creepList")
                .executes(COMMAND);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();
        ServerActionManager.getManager(playerEntity.world).listOptions(context.getSource());
        return 0;
    }
}
