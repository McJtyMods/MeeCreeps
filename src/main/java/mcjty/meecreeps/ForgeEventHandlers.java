package mcjty.meecreeps;

import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onWorldTick(TickEvent.ServerTickEvent event) {
        ServerActionManager.tick();
    }
}
