package mcjty.meecreeps;

import mcjty.lib.McJtyLib;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class CommandHandler {

    public static final String CMD_CANCEL_PORTAL = "cancel_portal";
    public static final String CMD_DELETE_DESTINATION = "delete_dest";
    public static final String CMD_SET_CURRENT = "set_current";
    public static final String CMD_RESUME_ACTION = "resume_action";
    public static final String CMD_CANCEL_ACTION = "cancel_action";

    public static void registerCommands() {
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_CANCEL_PORTAL, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            TeleportationTools.cancelPortalPair(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_DELETE_DESTINATION, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            PortalGunItem.addDestination(heldItem, null, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_SET_CURRENT, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            PortalGunItem.setCurrentDestination(heldItem, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_RESUME_ACTION, (player, arguments) -> {
            ServerActionManager.getManager().resumeAction((EntityPlayerMP) player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_CANCEL_ACTION, (player, arguments) -> {
            ServerActionManager.getManager().cancelAction((EntityPlayerMP) player, arguments.getInt());
            return true;
        });
    }
}
