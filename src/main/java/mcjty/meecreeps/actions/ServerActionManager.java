package mcjty.meecreeps.actions;

import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class ServerActionManager {

    private static List<ActionOptions> options = new ArrayList<>();

    public static void createActionOptions(World world, BlockPos pos, EntityPlayer player) {
        List<MeeCreepActionType> types = new ArrayList<>();
        types.add(MeeCreepActionType.ACTION_HARVEST);
        types.add(MeeCreepActionType.ACTION_PICKUP_ITEMS);
        types.add(MeeCreepActionType.ACTION_PLACE_TORCHES);
        options.add(new ActionOptions(types, pos, world.provider.getDimension(), player.getUniqueID()));
    }

    public static void performAction(ActionOptions option, MeeCreepActionType type) {
        System.out.println("ServerActionManager.performAction: " + type.getDescription());
    }

    public static void cancelAction(ActionOptions option) {
        System.out.println("ServerActionManager.cancelAction");
    }


    public static void tick() {
        List<ActionOptions> newlist = new ArrayList<>();
        for (ActionOptions option : options) {
            if (option.tick()) {
                MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
                EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(option.getPlayerId());
                if (player == null) {
                    // Player is no longer there. Discard this option
                    // @todo despawn meecreeps
                } else {
                    PacketHandler.INSTANCE.sendTo(new PacketActionOptionToClient(option), player);
                }
            } else {
                newlist.add(option);
            }
        }
        options = newlist;
    }
}
