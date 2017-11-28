package mcjty.meecreeps.commands;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandClearActions implements ICommand {


    @Override
    public String getName() {
        return "clear_actions";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "clear_actions";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        ServerActionManager.getManager().clearOptions();
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        if (sender instanceof EntityPlayerMP) {
            return ((EntityPlayerMP) sender).capabilities.isCreativeMode && sender.canUseCommand(2, "");
        } else {
            return sender.canUseCommand(2, "clear_actions");
        }
    }


    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }


}
