package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.teleport.PacketSetDestination;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAskName extends GuiScreen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private int guiLeft;
    private int guiTop;

    private String text = "";
    private int cursor = 0;

    public static int destinationIndex;
    public static TeleportDestination destination;

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
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_BACK) {
            if (!text.isEmpty() && cursor > 0) {
                text = text.substring(0, cursor-1) + text.substring(cursor);
                cursor--;
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (cursor < text.length()) {
                text = text.substring(0, cursor) + text.substring(cursor+1);
            }
        } else if (keyCode == Keyboard.KEY_HOME) {
            cursor = 0;
        } else if (keyCode == Keyboard.KEY_END) {
            cursor = text.length();
        } else if (keyCode == Keyboard.KEY_LEFT) {
            if (cursor > 0) {
                cursor--;
            }
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (cursor < text.length()) {
                cursor++;
            }
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
        } else if (keyCode == Keyboard.KEY_RETURN) {
            destination = new TeleportDestination(text, destination.getDimension(), destination.getPos(), destination.getSide());
            MeeCreepsMessages.INSTANCE.sendToServer(new PacketSetDestination(destination, destinationIndex));
            close();
        } else if (typedChar != 0) {
            if (text.length() < 15) {
                text = text.substring(0, cursor) + typedChar + text.substring(cursor);
                cursor++;
            }
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
        String label = I18n.format("message.meecreeps.gui.name_label") + " ";
        mc.fontRenderer.drawString(label + text, guiLeft+15, guiTop+7, 0);

        int w = mc.fontRenderer.getStringWidth(text.substring(0, cursor));
        int xx = guiLeft+10+mc.fontRenderer.getStringWidth(label);
        int yy = guiTop+3;
        Gui.drawRect(xx + 5 + w, yy + 2, xx + 5 + w + 1, yy + 14, 0xff000000);
    }
}
