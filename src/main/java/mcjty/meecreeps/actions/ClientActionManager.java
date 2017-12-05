package mcjty.meecreeps.actions;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public class ClientActionManager {

    public static ActionOptions lastOptions = null;

    public static void showActionOptions(ActionOptions options, int guiid) {
        EntityPlayer player = MeeCreeps.proxy.getClientPlayer();
        World worldIn = MeeCreeps.proxy.getClientWorld();
        BlockPos pos = options.getTargetPos();
        lastOptions = options;
        player.openGui(MeeCreeps.instance, guiid, worldIn, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void showProblem(String message, String... parameters) {
        EntityPlayer player = MeeCreeps.proxy.getClientPlayer();
        World worldIn = MeeCreeps.proxy.getClientWorld();
        BlockPos pos = player.getPosition();
        String translated = I18n.format(message, parameters);
        BalloonRenderer.messages.add(Pair.of(Config.balloonTimeout*2, translated));

    }
}
