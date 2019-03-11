package mcjty.meecreeps.render;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
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

    private static List<Pair<Integer, String>> messages = new ArrayList<>();

    private static String lastMessage = "";

    public static void addMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        List<String> strings = mc.fontRenderer.listFormattedStringToWidth(message, 230);
        for (String s : strings) {
            BalloonRenderer.messages.add(Pair.of(ConfigSetup.messageTimeout.get()*2, s));
        }
        lastMessage = message;
    }

    public static void repeatLast() {
        if (!lastMessage.isEmpty()) {
            addMessage(lastMessage);
        }
    }

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

        if (ConfigSetup.messageX.get() > 0) {
            guiLeft = width * ConfigSetup.messageX.get() / 100;
        } else if (ConfigSetup.messageX.get() < 0) {
            guiLeft = (width + width * ConfigSetup.messageX.get() / 100) - WIDTH;
        } else {
            guiLeft = (width - WIDTH) / 2;
        }

        if (ConfigSetup.messageY.get() > 0) {
            guiTop = height * ConfigSetup.messageY.get() / 100;
        } else if (ConfigSetup.messageY.get() < 0) {
            guiTop = (height + height * ConfigSetup.messageY.get() / 100) - HEIGHT;
        } else {
            guiTop = (height - HEIGHT) / 2;
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();

        mc.getTextureManager().bindTexture(gui_top);
        mcjty.lib.client.RenderHelper.drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        for (int i = 0; i < messages.size()-1 ; i++) {
            mcjty.lib.client.RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 10, WIDTH, 15);
            y += 14;
        }
        mcjty.lib.client.RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);

        List<Pair<Integer, String>> newMessages = new ArrayList<>();

        y = guiTop+7;
        for (Pair<Integer, String> pair : messages) {
            String msg = pair.getRight();
            mcjty.lib.client.RenderHelper.renderText(mc, guiLeft+15, y, msg, 0);
            y += 14;
            if (pair.getLeft() > 0) {
                newMessages.add(Pair.of(pair.getLeft()-1, msg));
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
