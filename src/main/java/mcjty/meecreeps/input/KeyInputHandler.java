package mcjty.meecreeps.input;

import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.repeatLastMessage.isPressed()) {
            BalloonRenderer.repeatLast();
        }
    }
}
