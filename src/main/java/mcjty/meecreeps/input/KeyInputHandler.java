package mcjty.meecreeps.input;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.render.BalloonRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = MeeCreeps.MODID, value = Dist.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public static void key(InputEvent.Key e) {
        while (KeyBindings.REPEAT.consumeClick())
            BalloonRenderer.repeatLast();
    }
}
