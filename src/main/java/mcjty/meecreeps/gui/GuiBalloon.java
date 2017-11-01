package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiBalloon extends GuiScreen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private int guiLeft;
    private int guiTop;

    public static String message;

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void close() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        close();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(gui_top);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);
        mc.fontRenderer.drawString(message, guiLeft+15, guiTop+7, 0);
    }
}
