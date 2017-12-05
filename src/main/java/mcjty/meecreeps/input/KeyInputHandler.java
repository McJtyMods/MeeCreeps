package mcjty.meecreeps.input;

import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBindings.repeatLastMessage.isPressed()) {
            BalloonRenderer.repeatLast();
        }
    }
}
