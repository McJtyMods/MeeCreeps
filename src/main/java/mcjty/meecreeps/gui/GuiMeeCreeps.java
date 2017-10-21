package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiMeeCreeps extends GuiScreen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");
    private final int OPTION_DISTANCE = 14;

    private int guiLeft;
    private int guiTop;

    private ActionOptions options;
    private boolean confirmedAction = false;

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;

        options = ClientActionManager.lastOptions;
        confirmedAction = false;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void closeThis() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void onGuiClosed() {
        if (!confirmedAction) {
            PacketHandler.INSTANCE.sendToServer(new PacketCancelAction(options));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int y = (mouseY-guiTop-21) / OPTION_DISTANCE;
            if (y < 0 || y >= options.getActionOptions().size()) {
            } else {
                confirmedAction = true;
                PacketHandler.INSTANCE.sendToServer(new PacketPerformAction(options, options.getActionOptions().get(y)));
            }
            closeThis();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(gui_top);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;
        for (int i = 0 ; i <= options.getActionOptions().size() ; i++) {
            drawTexturedModalRect(guiLeft, y, 0, 10, WIDTH, 15);
            y += OPTION_DISTANCE;
        }
        drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);
        mc.fontRenderer.drawString("What can I do for you?", guiLeft+15, guiTop+7, 0);
        y = guiTop+21;
        for (MeeCreepActionType type : options.getActionOptions()) {
            int color = 0xff666600;
            if (mouseY > y && mouseY < y+ OPTION_DISTANCE) {
                color = 0xff22dd00;
            }
            mc.fontRenderer.drawString(type.getDescription(), guiLeft+40, y, color);
            y += OPTION_DISTANCE;
        }
        int color = 0xff666600;
        if (mouseY > y && mouseY < y+ OPTION_DISTANCE) {
            color = 0xff22dd00;
        }
        mc.fontRenderer.drawString("Never mind...", guiLeft+40, y, color);
    }
}
