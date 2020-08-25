package mcjty.meecreeps.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandClearActions implements Command<CommandSource> {

    private static final CommandClearActions COMMAND = new CommandClearActions();
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("clearCreeps")
                .executes(COMMAND);
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity playerEntity = context.getSource().asPlayer();

        if (playerEntity.isCreative() && playerEntity.hasPermissionLevel(2)) {
            ServerActionManager.getManager(playerEntity.world).clearOptions(context.getSource(), null);
        } else {
            ServerActionManager.getManager(playerEntity.world).clearOptions(context.getSource(), playerEntity);
        }

// todo: reeval that the top does the right thing

//        if (context.getSource().asPlayer() instanceof EntityPlayerMP) {
//            if (((EntityPlayerMP) sender).capabilities.isCreativeMode && sender.canUseCommand(2, "")) {
//                ServerActionManager.getManager().clearOptions(sender, null);
//            } else {
//                ServerActionManager.getManager().clearOptions(sender, (EntityPlayerMP) sender);
//            }
//        } else {
//            if (sender.canUseCommand(2, "creep_clear")) {
//                ServerActionManager.getManager().clearOptions(sender, null);
//            } else {
//                sender.sendMessage(new TextComponentString(TextFormatting.RED + "You have no permission for this command!"));
//            }
//        }
        return 0;
    }
}
