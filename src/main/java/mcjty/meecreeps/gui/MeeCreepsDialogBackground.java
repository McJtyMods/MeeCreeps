package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Draws the original dialog texture in strips so its rounded corners stay intact. */
public final class MeeCreepsDialogBackground {
    public static final int WIDTH = 256;
    public static final int ROW_HEIGHT = 14;
    private static final int TOP_HEIGHT = 10;
    private static final int BOTTOM_HEIGHT = 15;
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private MeeCreepsDialogBackground() {
    }

    public static int height(int bodyRows) {
        return TOP_HEIGHT + bodyRows * ROW_HEIGHT + BOTTOM_HEIGHT;
    }

    public static void render(GuiGraphicsExtractor graphics, int x, int y, int bodyRows) {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, WIDTH, TOP_HEIGHT, 256, 256);
        y += TOP_HEIGHT;
        for (int row = 0; row < bodyRows; row++) {
            graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 10, WIDTH, ROW_HEIGHT, 256, 256);
            y += ROW_HEIGHT;
        }
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 25, WIDTH, BOTTOM_HEIGHT, 256, 256);
    }
}
