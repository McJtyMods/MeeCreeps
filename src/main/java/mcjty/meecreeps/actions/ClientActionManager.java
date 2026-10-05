package mcjty.meecreeps.actions;

import mcjty.meecreeps.gui.GuiMeeCreeps;
import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

public class ClientActionManager {
    public static ActionOptions lastOptions;

    public static void showActionOptions(ActionOptions options, int guiid) {
        lastOptions = options;
        Minecraft.getInstance().setScreen(new GuiMeeCreeps(guiid));
    }

    public static void showProblem(String message, String... parameters) {
        BalloonRenderer.addMessage(I18n.get(message, (Object[]) parameters));
    }
}
