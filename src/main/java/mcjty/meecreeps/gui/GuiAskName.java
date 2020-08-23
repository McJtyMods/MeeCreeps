package mcjty.meecreeps.gui;

import mcjty.lib.client.RenderHelper;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.java.games.input.Keyboard;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiAskName extends Screen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");

    private int guiLeft;
    private int guiTop;

    private String text = "";
    private int cursor = 0;

    public static int destinationIndex;
    public static TeleportDestination destination;

    protected GuiAskName() {
        super(new StringTextComponent(""));
    }

    @Override
    public void init() {
        super.init();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void close() {
        this.getMinecraft().displayGuiScreen(null);
        if (this.getMinecraft().currentScreen == null) {
            this.getMinecraft().setGameFocused(true);
        }
    }


    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!text.isEmpty() && cursor > 0) {
                text = text.substring(0, cursor-1) + text.substring(cursor);
                cursor--;
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (cursor < text.length()) {
                text = text.substring(0, cursor) + text.substring(cursor+1);
            }
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            cursor = 0;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            cursor = text.length();
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (cursor > 0) {
                cursor--;
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (cursor < text.length()) {
                cursor++;
            }
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            destination = new TeleportDestination(text, destination.getDimension(), destination.getPos(), destination.getSide());
            // todo: fix
//            MeeCreepsMessages.INSTANCE.sendToServer(new PacketSetDestination(destination, destinationIndex));
            close();
        } else if (typedChar != 0) {
            if (text.length() < 15) {
                text = text.substring(0, cursor) + typedChar + text.substring(cursor);
                cursor++;
            }
        }

        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        close();

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        getMinecraft().getTextureManager().bindTexture(gui_top);
        RenderHelper.drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);
        String label = I18n.format("message.meecreeps.gui.name_label") + " ";
        getMinecraft().fontRenderer.drawString(label + text, guiLeft+15, guiTop+7, 0);

        int w = getMinecraft().fontRenderer.getStringWidth(text.substring(0, cursor));
        int xx = guiLeft+10+getMinecraft().fontRenderer.getStringWidth(label);
        int yy = guiTop+3;
        // todo: fix this
//        Gui.drawRect(xx + 5 + w, yy + 2, xx + 5 + w + 1, yy + 14, 0xff000000);
    }

}
