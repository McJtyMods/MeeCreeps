package mcjty.meecreeps;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CommandHandler {

    public static final String CMD_CANCEL_PORTAL = "cancel_portal";
    public static final String CMD_DELETE_DESTINATION = "delete_dest";
    public static final String CMD_SET_CURRENT = "set_current";
    public static final String CMD_RESUME_ACTION = "resume_action";
    public static final String CMD_CANCEL_ACTION = "cancel_action";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<Integer> PARAM_ID = new Key<>("id", Type.INTEGER);

    public static void registerCommands() {
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_CANCEL_PORTAL, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            TeleportationTools.cancelPortalPair(player, arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_DELETE_DESTINATION, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            PortalGunItem.addDestination(heldItem, null, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_SET_CURRENT, (player, arguments) -> {
            ItemStack heldItem = PortalGunItem.getGun(player);
            if (heldItem.isEmpty()) return false; // Something went wrong
            PortalGunItem.setCurrentDestination(heldItem, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_RESUME_ACTION, (player, arguments) -> {
            ServerActionManager.getManager().resumeAction((ServerPlayerEntity) player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(MeeCreeps.MODID, CMD_CANCEL_ACTION, (player, arguments) -> {
            ServerActionManager.getManager().cancelAction((ServerPlayerEntity) player, arguments.get(PARAM_ID));
            return true;
        });
    }
}
