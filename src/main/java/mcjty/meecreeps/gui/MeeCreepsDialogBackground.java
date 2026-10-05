package mcjty.meecreeps.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Draws the original dialog texture in strips so its rounded corners stay intact. */
public final class MeeCreepsDialogBackground {
    public static final int WIDTH = 256;
    public static final int ROW_HEIGHT = 14;
    private static final int TOP_HEIGHT = 10;
    private static final int BOTTOM_HEIGHT = 15;
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private MeeCreepsDialogBackground() {
    }

    public static int height(int bodyRows) {
        return TOP_HEIGHT + bodyRows * ROW_HEIGHT + BOTTOM_HEIGHT;
    }

    public static void render(GuiGraphics graphics, int x, int y, int bodyRows) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, x, y, 0, 0, WIDTH, TOP_HEIGHT);
        y += TOP_HEIGHT;
        for (int row = 0; row < bodyRows; row++) {
            graphics.blit(TEXTURE, x, y, 0, 10, WIDTH, ROW_HEIGHT);
            y += ROW_HEIGHT;
        }
        graphics.blit(TEXTURE, x, y, 0, 25, WIDTH, BOTTOM_HEIGHT);
        RenderSystem.disableBlend();
    }
}
