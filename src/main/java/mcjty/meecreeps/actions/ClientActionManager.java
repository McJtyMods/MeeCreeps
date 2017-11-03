package mcjty.meecreeps.actions;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.gui.GuiBalloon;
import mcjty.meecreeps.proxy.GuiProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClientActionManager {

    public static ActionOptions lastOptions = null;

    public static void showActionOptions(ActionOptions options, int guiid) {
        EntityPlayer player = MeeCreeps.proxy.getClientPlayer();
        World worldIn = MeeCreeps.proxy.getClientWorld();
        BlockPos pos = options.getTargetPos();
        lastOptions = options;
        player.openGui(MeeCreeps.instance, guiid, worldIn, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void showProblem(String message) {
        EntityPlayer player = MeeCreeps.proxy.getClientPlayer();
        World worldIn = MeeCreeps.proxy.getClientWorld();
        BlockPos pos = player.getPosition();
        GuiBalloon.message = message;
        player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, worldIn, pos.getX(), pos.getY(), pos.getZ());

    }
}
