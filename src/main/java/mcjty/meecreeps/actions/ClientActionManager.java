package mcjty.meecreeps.actions;

import mcjty.meecreeps.gui.GuiMeeCreeps;
import mcjty.meecreeps.render.BalloonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class ClientActionManager {

    public static ActionOptions lastOptions = null;

    public static void showActionOptions(ActionOptions options, int guiid) {
        // todo: this can't be right...
        lastOptions = options;
        Minecraft.getInstance().displayGuiScreen(new GuiMeeCreeps(guiid));
    }

    public static void showProblem(String message, String... parameters) {
        String translated = I18n.format(message, parameters);
        BalloonRenderer.addMessage(translated);
    }
}
