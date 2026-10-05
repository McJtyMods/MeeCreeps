package mcjty.meecreeps.input;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.InputEvent;

@Mod.EventBusSubscriber(modid = MeeCreeps.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void key(InputEvent.Key e) {
        while (KeyBindings.REPEAT.consumeClick())
            BalloonRenderer.repeatLast();
    }
}
