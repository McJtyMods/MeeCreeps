package mcjty.meecreeps.commands;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class CommandClearActions implements ICommand {


    @Override
    public String getName() {
        return "creep_clear";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "creep_clear";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            if (((EntityPlayerMP) sender).capabilities.isCreativeMode && sender.canUseCommand(2, "")) {
                ServerActionManager.getManager().clearOptions(sender, null);
            } else {
                ServerActionManager.getManager().clearOptions(sender, (EntityPlayerMP) sender);
            }
        } else {
            if (sender.canUseCommand(2, "creep_clear")) {
                ServerActionManager.getManager().clearOptions(sender, null);
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "You have no permission for this command!"));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
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
