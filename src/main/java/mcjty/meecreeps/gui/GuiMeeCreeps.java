package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiMeeCreeps extends GuiScreen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private int guiLeft;
    private int guiTop;

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

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(gui_top);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+10, 0, 10, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+20, 0, 10, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+30, 0, 10, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+40, 0, 10, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+50, 0, 10, WIDTH, 10);
        drawTexturedModalRect(guiLeft, guiTop+60, 0, 30, WIDTH, 10);
        mc.fontRenderer.drawString("What can I do for you?", guiLeft+15, guiTop+7, 0);
        mc.fontRenderer.drawString("Chop that nearby tree", guiLeft+40, guiTop+19, 0xff666600);
        mc.fontRenderer.drawString("Harvest those crops", guiLeft+40, guiTop+31, 0xff666600);
        mc.fontRenderer.drawString("Light the place", guiLeft+40, guiTop+43, 0xff666600);
    }
}
