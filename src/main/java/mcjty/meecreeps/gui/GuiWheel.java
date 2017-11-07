package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.proxy.GuiProxy;
import mcjty.meecreeps.render.RenderHelper;
import mcjty.meecreeps.teleport.PacketDeleteDestination;
import mcjty.meecreeps.teleport.PacketSetCurrent;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiWheel extends GuiScreen {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 160;

    private int guiLeft;
    private int guiTop;

    private int lastSelected = -1;

    private static final ResourceLocation background = new ResourceLocation(MeeCreeps.MODID, "textures/gui/wheel.png");
    private static final ResourceLocation hilight = new ResourceLocation(MeeCreeps.MODID, "textures/gui/wheel_hilight.png");

    public static BlockPos selectedBlock;
    public static EnumFacing selectedSide;

    public GuiWheel() {
    }


    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;
    }

    private static boolean isKeyDown(KeyBinding key) {
        int i = key.getKeyCode();
        return ((i != 0) && (i < 256)) ? ((i < 0) ? Mouse.isButtonDown(i + 100) : Keyboard.isKeyDown(i)) : false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
            if (lastSelected != -1) {
                ItemStack heldItem = PortalGunItem.getGun(mc.player);
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(heldItem);
                if (destinations.get(lastSelected) != null) {
                    PacketHandler.INSTANCE.sendToServer(new PacketDeleteDestination(lastSelected));
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int cx = mouseX - guiLeft - WIDTH / 2;
        int cy = mouseY - guiTop - HEIGHT / 2;

        int q = getSelectedSection(cx, cy);
        if (q == -1) {
            closeThis();
        } else {
            ItemStack heldItem = PortalGunItem.getGun(mc.player);
            if (!heldItem.isEmpty()) {
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(heldItem);
                if (destinations.get(q) != null) {
                    PacketHandler.INSTANCE.sendToServer(new PacketSetCurrent(q));
                } else {
                    BlockPos bestPosition = TeleportationTools.findBestPosition(mc.world, selectedBlock, selectedSide);
                    if (bestPosition == null) {
                        GuiBalloon.message = "Can't find a good spot to make a portal!";
                        closeThis();
                        mc.player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, mc.world, selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());
                        return;
                    } else {
                        GuiAskName.destinationIndex = q;
                        GuiAskName.destination = new TeleportDestination("", mc.world.provider.getDimension(), bestPosition, selectedSide);
                        closeThis();
                        mc.player.openGui(MeeCreeps.instance, GuiProxy.GUI_ASKNAME, mc.world, selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());
                        return;
                    }

                }
            }
            closeThis();
        }
    }

    private void closeThis() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    private static final List<Pair<Integer, Integer>> iconOffsets = new ArrayList<>();

    static {
        iconOffsets.add(Pair.of(78 + 8, 8));
        iconOffsets.add(Pair.of(107 + 12, 22 + 19));
        iconOffsets.add(Pair.of(107 + 12, 78 + 9));
        iconOffsets.add(Pair.of(78 + 9, 108 + 11));
        iconOffsets.add(Pair.of(23 + 18, 107 + 11));
        iconOffsets.add(Pair.of(0 + 10, 78 + 9));
        iconOffsets.add(Pair.of(0 + 9, 22 + 19));
        iconOffsets.add(Pair.of(22 + 19, 8));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

        int cx = mouseX - guiLeft - WIDTH / 2;
        int cy = mouseY - guiTop - HEIGHT / 2;
        int offset = 8 / 2;
        lastSelected = getSelectedSection(cx, cy);
        if (lastSelected != -1) {
            drawSelectedSection(offset, lastSelected, 0);
            if (lastSelected < 8) {
                drawTooltip(lastSelected);
            }
        }

        int currentDestination = PortalGunItem.getCurrentDestination(PortalGunItem.getGun(mc.player));
        if (currentDestination != -1) {
            drawSelectedSection(offset, currentDestination, 128);
        }

        drawIcons(offset, lastSelected);

        if (lastSelected != -1) {
            if (lastSelected < 8) {
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getMinecraft().player));
                TeleportDestination destination = destinations.get(lastSelected);
                List<String> tooltips = new ArrayList<>();
                if (destination == null) {
                    tooltips.add(TextFormatting.BLUE + "Click: " + TextFormatting.WHITE + "to set current location as destination");
                } else {
                    tooltips.add(TextFormatting.BLUE + "Click: " + TextFormatting.WHITE + "to set this destination as current");
                    tooltips.add(TextFormatting.BLUE + "Del: " + TextFormatting.WHITE + "to remove this destination");
                }
                drawHoveringText(tooltips, mouseX, mouseY);
            }
        }
    }

    private void drawIcons(int offset, int q) {

        List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getMinecraft().player));
        for (int i = 0; i < 8; i++) {
            String id = destinations.get(i) == null ? "" : destinations.get(i).getName();
            int offs = (i - offset + 8) % 8;

            double angle = Math.PI * 2.0 * offs / 8 - Math.PI / 2.0 + Math.PI / 8.0;
            int tx = (int) (guiLeft + 80 + 60 * Math.cos(angle));
            int ty = (int) (guiTop + 80 + 60 * Math.sin(angle));
            RenderHelper.renderText(mc, tx - mc.fontRenderer.getStringWidth(id) / 2, ty - mc.fontRenderer.FONT_HEIGHT / 2, id);
        }
    }

    private void renderTooltipText(String desc) {
        int width = mc.fontRenderer.getStringWidth(desc);
        int x = guiLeft + (160 - width) / 2;
        int y = guiTop + HEIGHT + 5;
        RenderHelper.renderText(mc, x, y, desc);
    }


    private void drawTooltip(int q) {
//        boolean extended = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getMinecraft().player));
        TeleportDestination destination = destinations.get(q);
        if (destination == null) {
            renderTooltipText("Destination not set");
        } else {
            BlockPos p = destination.getPos();
            if (destination.getDimension() == Minecraft.getMinecraft().world.provider.getDimension()) {
                double dist = Math.sqrt(p.distanceSq(Minecraft.getMinecraft().player.getPosition()));
                renderTooltipText(p.getX() + "," + p.getY() + "," + p.getZ() + " (" + (int) dist + " blocks)");
            } else {
                renderTooltipText(p.getX() + "," + p.getY() + "," + p.getZ() + " (dim " + destination.getDimension() + ")");
            }
        }
    }

    private void drawSelectedSection(int offset, int q, int voffset) {
        mc.getTextureManager().bindTexture(hilight);
        switch ((q - offset + 8) % 8) {
            case 0:
                drawTexturedModalRect(guiLeft + 78, guiTop, 0, voffset, 63, 63);
                break;
            case 1:
                drawTexturedModalRect(guiLeft + 107, guiTop + 22, 64, voffset, 63, 63);
                break;
            case 2:
                drawTexturedModalRect(guiLeft + 107, guiTop + 78, 128, voffset, 63, 63);
                break;
            case 3:
                drawTexturedModalRect(guiLeft + 78, guiTop + 108, 192, voffset, 63, 63);
                break;
            case 4:
                drawTexturedModalRect(guiLeft + 23, guiTop + 107, 0, voffset+64, 63, 63);
                break;
            case 5:
                drawTexturedModalRect(guiLeft, guiTop + 78, 64, voffset+64, 63, 63);
                break;
            case 6:
                drawTexturedModalRect(guiLeft, guiTop + 22, 128, voffset+64, 63, 63);
                break;
            case 7:
                drawTexturedModalRect(guiLeft + 22, guiTop, 192, voffset+64, 63, 63);
                break;
        }
    }


    private int getSelectedSection(int cx, int cy) {
        double dist = Math.sqrt(cx * cx + cy * cy);
        if (dist < 37 || dist > 80) {
            return -1;
        }

        int q = -1;
        if (cx >= 0 && cy < 0 && Math.abs(cx) < Math.abs(cy)) {
            q = 0;
        } else if (cx >= 0 && cy < 0 && Math.abs(cx) >= Math.abs(cy)) {
            q = 1;
        } else if (cx >= 0 && cy >= 0 && Math.abs(cx) >= Math.abs(cy)) {
            q = 2;
        } else if (cx >= 0 && cy >= 0 && Math.abs(cx) < Math.abs(cy)) {
            q = 3;
        } else if (cx < 0 && cy >= 0 && Math.abs(cx) < Math.abs(cy)) {
            q = 4;
        } else if (cx < 0 && cy >= 0 && Math.abs(cx) >= Math.abs(cy)) {
            q = 5;
        } else if (cx < 0 && cy < 0 && Math.abs(cx) >= Math.abs(cy)) {
            q = 6;
        } else if (cx < 0 && cy < 0 && Math.abs(cx) < Math.abs(cy)) {
            q = 7;
        }
        int offset = 8 / 2;
        return (q + offset) % 8;
    }
}
