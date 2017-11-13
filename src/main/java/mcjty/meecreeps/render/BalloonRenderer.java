package mcjty.meecreeps.render;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class BalloonRenderer {

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    public static List<Pair<Integer, String>> messages = new ArrayList<>();

    public static void renderBalloon() {
        if (messages.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();

        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(gui_top);

        ScaledResolution scaledresolution = new ScaledResolution(mc);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
//        double sw = scaledresolution.getScaledWidth_double();
//        double sh = scaledresolution.getScaledHeight_double();

//        double scale = 1.0;
//        setupOverlayRendering(sw * scale, sh * scale);

        int WIDTH = 256;
        int HEIGHT = (messages.size() * 14) + 10 + 15;
        int guiLeft;
        int guiTop;

        if (Config.balloonX > 0) {
            guiLeft = width * Config.balloonX / 100;
        } else if (Config.balloonX < 0) {
            guiLeft = (width + width * Config.balloonX / 100) - WIDTH;
        } else {
            guiLeft = (width - WIDTH) / 2;
        }

        if (Config.balloonY > 0) {
            guiTop = height * Config.balloonY / 100;
        } else if (Config.balloonY < 0) {
            guiTop = (height + height * Config.balloonY / 100) - HEIGHT;
        } else {
            guiTop = (height - HEIGHT) / 2;
        }

        System.out.println("guiLeft = " + guiLeft);
        System.out.println("guiTop = " + guiTop);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();

        mc.getTextureManager().bindTexture(gui_top);
        RenderHelper.drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        for (int i = 0; i < messages.size()-1 ; i++) {
            RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 10, WIDTH, 15);
            y += 14;
        }
        RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);

        List<Pair<Integer, String>> newMessages = new ArrayList<>();

        y = guiTop+7;
        for (Pair<Integer, String> pair : messages) {
            RenderHelper.renderText(mc, guiLeft+15, y, pair.getRight(), 0);
            y += 14;
            if (pair.getLeft() > 0) {
                newMessages.add(Pair.of(pair.getLeft()-1, pair.getRight()));
            }
        }
        messages = newMessages;

        GlStateManager.popMatrix();
    }

    public static void setupOverlayRendering(double sw, double sh) {
        GlStateManager.clear(256);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, sw, sh, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

}
