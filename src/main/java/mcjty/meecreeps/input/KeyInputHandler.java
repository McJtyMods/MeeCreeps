package mcjty.meecreeps.input;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.render.BalloonRenderer;

public class KeyInputHandler {
    public static void key() {
        while (KeyBindings.REPEAT.consumeClick())
            BalloonRenderer.repeatLast();
    }
}
