package mcjty.meecreeps.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.client.GuiTools;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.DimensionId;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ClientActionManager;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.setup.GuiProxy;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiWheel extends Screen {

    private static final int WIDTH = 160;
    private static final int HEIGHT = 160;

    private int guiLeft;
    private int guiTop;

    private int lastSelected = -1;

    private static final ResourceLocation background = new ResourceLocation(MeeCreeps.MODID, "textures/gui/wheel.png");
    private static final ResourceLocation hilight = new ResourceLocation(MeeCreeps.MODID, "textures/gui/wheel_hilight.png");

    public static BlockPos selectedBlock;
    public static Direction selectedSide;

    public GuiWheel() {
        super(new StringTextComponent(""));
    }

    @Override
    public void init() {
        super.init();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;
    }

// not used @since 1.12
//    private static boolean isKeyDown(KeyBinding key) {
//        int i = key.getKey().getKeyCode();
//        return ((i != 0) && (i < 256)) ? ((i < 0) ? Mouse.isButtonDown(i + 100) : Keyboard.isKeyDown(i)) : false;
//    }

    @Override
    public boolean charTyped(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (lastSelected != -1) {
                ItemStack heldItem = PortalGunItem.getGun(getMinecraft().player);
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(heldItem);
                if (destinations.get(lastSelected) != null) {
                    PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_DELETE_DESTINATION,
                            TypedMap.builder().put(CommandHandler.PARAM_ID, lastSelected).build()));
                }
            }
        }

        return super.charTyped(typedChar, keyCode);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        int cx = (int) (mouseX - guiLeft - WIDTH / 2);
        int cy = (int) (mouseY - guiTop - HEIGHT / 2);

        int q = getSelectedSection(cx, cy);
        if (q == -1) {
            closeThis();
        } else {
            ItemStack heldItem = PortalGunItem.getGun(getMinecraft().player);
            if (!heldItem.isEmpty()) {
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(heldItem);
                if (destinations.get(q) != null) {
                    PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_SET_CURRENT,
                            TypedMap.builder().put(CommandHandler.PARAM_ID, q).build()));
                } else {
                    BlockPos bestPosition = TeleportationTools.findBestPosition(getMinecraft().world, selectedBlock, selectedSide);
                    if (bestPosition == null) {
//                        GuiBalloon.message = "Can't find a good spot to make a portal!";
                        closeThis();
                        ClientActionManager.showProblem("message.meecreeps.cant_find_portal_spot");
//                        getMinecraft().player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, getMinecraft().world, selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());
                        return false;
                    } else {
                        GuiAskName.destinationIndex = q;
                        GuiAskName.destination = new TeleportDestination("", DimensionId.fromWorld(getMinecraft().world), bestPosition, selectedSide);
                        closeThis();
                        getMinecraft().displayGuiScreen(new GuiAskName());
//                        getMinecraft().displayGuiScreen(MeeCreeps.instance, GuiProxy.GUI_ASKNAME, getMinecraft().world, selectedBlock.getX(), selectedBlock.getY(), selectedBlock.getZ());
                        return false;
                    }

                }
            }
            closeThis();
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void closeThis() {
        this.getMinecraft().displayGuiScreen(null);
        if (this.getMinecraft().currentScreen == null) {
            this.getMinecraft().setGameFocused(true);
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        GlStateManager.enableBlend();
        getMinecraft().getTextureManager().bindTexture(background);
        RenderHelper.drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, HEIGHT);

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

        int currentDestination = PortalGunItem.getCurrentDestination(PortalGunItem.getGun(getMinecraft().player));
        if (currentDestination != -1) {
            drawSelectedSection(offset, currentDestination, 128);
        }

        drawIcons(offset, lastSelected);

        if (lastSelected != -1) {
            if (lastSelected < 8) {
                List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getInstance().player));
                TeleportDestination destination = destinations.get(lastSelected);
                List<String> tooltips = new ArrayList<>();
                if (destination == null) {
                    tooltips.add(TextFormatting.BLUE + "Click: " + TextFormatting.WHITE + "to set current location as destination");
                } else {
                    tooltips.add(TextFormatting.BLUE + "Click: " + TextFormatting.WHITE + "to set this destination as current");
                    tooltips.add(TextFormatting.BLUE + "Del: " + TextFormatting.WHITE + "to remove this destination");
                }

                // todo: ensure this works
                GuiUtils.drawHoveringText(tooltips, mouseX, mouseY, width, height, 150, font);
            }
        }
    }

    private void drawIcons(int offset, int q) {

        List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getInstance().player));
        for (int i = 0; i < 8; i++) {
            String id = destinations.get(i) == null ? "" : destinations.get(i).getName();
            int offs = (i - offset + 8) % 8;

            double angle = Math.PI * 2.0 * offs / 8 - Math.PI / 2.0 + Math.PI / 8.0;
            int tx = (int) (guiLeft + 80 + 60 * Math.cos(angle));
            int ty = (int) (guiTop + 80 + 60 * Math.sin(angle));
            RenderHelper.renderText(tx - getMinecraft().fontRenderer.getStringWidth(id) / 2, ty - getMinecraft().fontRenderer.FONT_HEIGHT / 2, id);
        }
    }

    private void renderTooltipText(String desc) {
        int width = getMinecraft().fontRenderer.getStringWidth(desc);
        int x = guiLeft + (160 - width) / 2;
        int y = guiTop + HEIGHT + 5;
        RenderHelper.renderText(x, y, desc);
    }


    private void drawTooltip(int q) {
//        boolean extended = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        List<TeleportDestination> destinations = PortalGunItem.getDestinations(PortalGunItem.getGun(Minecraft.getInstance().player));
        TeleportDestination destination = destinations.get(q);
        if (destination == null) {
            renderTooltipText(I18n.format("message.meecreeps.gui.destination_not_set"));
        } else {
            BlockPos p = destination.getPos();

            if (destination.getDimension().equals(DimensionId.fromWorld(Minecraft.getInstance().world))) {
                double dist = Math.sqrt(p.distanceSq(Minecraft.getInstance().player.getPosition()));
                renderTooltipText(p.getX() + "," + p.getY() + "," + p.getZ() + " (" + (int) dist + " blocks)");
            } else {
                renderTooltipText(p.getX() + "," + p.getY() + "," + p.getZ() + " (dim " + destination.getDimension() + ")");
            }
        }
    }

    private void drawSelectedSection(int offset, int q, int voffset) {
        getMinecraft().getTextureManager().bindTexture(hilight);
        switch ((q - offset + 8) % 8) {
            case 0:
                RenderHelper.drawTexturedModalRect(guiLeft + 78, guiTop, 0, voffset, 63, 63);
                break;
            case 1:
                RenderHelper.drawTexturedModalRect(guiLeft + 107, guiTop + 22, 64, voffset, 63, 63);
                break;
            case 2:
                RenderHelper.drawTexturedModalRect(guiLeft + 107, guiTop + 78, 128, voffset, 63, 63);
                break;
            case 3:
                RenderHelper.drawTexturedModalRect(guiLeft + 78, guiTop + 108, 192, voffset, 63, 63);
                break;
            case 4:
                RenderHelper.drawTexturedModalRect(guiLeft + 23, guiTop + 107, 0, voffset+64, 63, 63);
                break;
            case 5:
                RenderHelper.drawTexturedModalRect(guiLeft, guiTop + 78, 64, voffset+64, 63, 63);
                break;
            case 6:
                RenderHelper.drawTexturedModalRect(guiLeft, guiTop + 22, 128, voffset+64, 63, 63);
                break;
            case 7:
                RenderHelper.drawTexturedModalRect(guiLeft + 22, guiTop, 192, voffset+64, 63, 63);
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
