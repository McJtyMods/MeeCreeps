package mcjty.meecreeps;

import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class CommandHandler {
    public static final String CMD_CANCEL_PORTAL = "cancel_portal";
    public static final String CMD_DELETE_DESTINATION = "delete_dest";
    public static final String CMD_SET_CURRENT = "set_current";
    public static final String CMD_RESUME_ACTION = "resume_action";
    public static final String CMD_CANCEL_ACTION = "cancel_action";

    public static void handle(ServerPlayer player, String command, int id, BlockPos pos) {
        if (CMD_RESUME_ACTION.equals(command)) {
            ServerActionManager.getManager().resumeAction(player, id);
        } else if (CMD_CANCEL_ACTION.equals(command)) {
            ServerActionManager.getManager().cancelAction(player, id);
        } else {
            var gun = PortalGunItem.getGun(player);
            if (gun.isEmpty()) return;
            if (CMD_CANCEL_PORTAL.equals(command) && pos != null) {
                TeleportationTools.cancelPortalPair(player, pos);
            } else if (id >= 0 && id < 8) {
                if (CMD_DELETE_DESTINATION.equals(command)) PortalGunItem.addDestination(gun, null, id);
                else if (CMD_SET_CURRENT.equals(command)) PortalGunItem.setCurrentDestination(gun, id);
            }
        }
    }
}
