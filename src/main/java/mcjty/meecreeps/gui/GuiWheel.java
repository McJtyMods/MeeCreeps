package mcjty.meecreeps.gui;

import mcjty.meecreeps.network.PacketServerCommand;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.teleport.PacketMakePortals;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GuiWheel extends Screen {
    private static final int SIZE = 160;
    private static final int SLOT_OFFSET = 4;
    private static final Identifier WHEEL = Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "textures/gui/wheel.png");
    private static final Identifier HIGHLIGHT = Identifier.fromNamespaceAndPath(MeeCreeps.MODID, "textures/gui/wheel_hilight.png");
    // Position of each 63x63 segment in the original highlight atlas, clockwise from the top.
    private static final int[][] SEGMENT_POSITIONS = {
            {78, 0}, {107, 22}, {107, 78}, {78, 108},
            {23, 107}, {0, 78}, {0, 22}, {22, 0}
    };

    public static BlockPos selectedBlock;
    public static Direction selectedSide;
    private int selected = -1;

    public GuiWheel() {
        super(Component.literal("Portal destinations"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        // Keep the world and HUD clear beneath this transparent overlay.
    }

    private int left() {
        return (width - SIZE) / 2;
    }

    private int top() {
        return (height - SIZE) / 2;
    }

    private int section(double x, double y) {
        double dx = x - left() - SIZE / 2.0;
        double dy = y - top() - SIZE / 2.0;
        double radius = Math.hypot(dx, dy);
        if (radius < 37 || radius > 80) {
            return -1;
        }
        int segment = Math.floorMod((int) Math.floor((Math.atan2(dy, dx) + Math.PI / 2) / (Math.PI / 4)), 8);
        return (segment + SLOT_OFFSET) % 8;
    }

    private void command(String cmd, int index) {
        MeeCreepsMessages.INSTANCE.sendToServer(new PacketServerCommand(cmd, index));
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y();
        int button = event.button();
        int index = section(x, y);
        if (index < 0) {
            onClose();
            return true;
        }
        var gun = PortalGunItem.getGun(minecraft.player);
        if (gun.isEmpty()) {
            onClose();
            return true;
        }
        var dest = PortalGunItem.getDestinations(gun).get(index);
        if (dest != null) {
            if (button == 1 && selectedBlock != null) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketMakePortals(selectedBlock, selectedSide, dest));
            } else {
                command(CommandHandler.CMD_SET_CURRENT, index);
            }
            onClose();
        } else if (selectedBlock != null) {
            BlockPos pos = TeleportationTools.findBestPosition(minecraft.level, selectedBlock, selectedSide);
            if (pos != null) {
                minecraft.gui.setScreen(new GuiAskName(index, new TeleportDestination("", minecraft.level.dimension(), pos, selectedSide)));
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int key = event.key();
        if ((key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) && selected >= 0) {
            command(CommandHandler.CMD_DELETE_DESTINATION, selected);
            return true;
        }
        return super.keyPressed(event);
    }

    private void drawSelectedSection(GuiGraphicsExtractor graphics, int slot, int textureRow) {
        int segment = Math.floorMod(slot - SLOT_OFFSET, 8);
        int[] pos = SEGMENT_POSITIONS[segment];
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, HIGHLIGHT, left() + pos[0], top() + pos[1],
                (segment % 4) * 64, textureRow + (segment / 4) * 64, 63, 63, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partial) {
        selected = section(mouseX, mouseY);
        var gun = PortalGunItem.getGun(minecraft.player);
        var destinations = PortalGunItem.getDestinations(gun);

        // Keep the world visible through the wheel's transparent center, as in 1.12.2.
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, WHEEL, left(), top(), 0, 0, SIZE, SIZE, 256, 256);
        if (selected >= 0) {
            drawSelectedSection(graphics, selected, 0);
        }
        int current = PortalGunItem.getCurrentDestination(gun);
        if (current >= 0 && current < 8 && destinations.get(current) != null) {
            drawSelectedSection(graphics, current, 128);
        }

        for (int slot = 0; slot < 8; slot++) {
            var destination = destinations.get(slot);
            if (destination == null) {
                continue;
            }
            int segment = Math.floorMod(slot - SLOT_OFFSET, 8);
            double angle = segment * Math.PI / 4 - Math.PI / 2 + Math.PI / 8;
            int x = (int) (left() + 80 + 60 * Math.cos(angle));
            int y = (int) (top() + 80 + 60 * Math.sin(angle));
            graphics.centeredText(font, destination.getName(), x, y - font.lineHeight / 2, 0xffffffff);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partial);

        if (selected >= 0) {
            var destination = destinations.get(selected);
            Component description;
            List<Component> tooltip = new ArrayList<>();
            if (destination == null) {
                description = Component.translatable("message.meecreeps.gui.destination_not_set");
                tooltip.add(hint("Click: ", "to set current location as destination"));
            } else {
                BlockPos pos = destination.getPos();
                String coordinates = pos.getX() + "," + pos.getY() + "," + pos.getZ();
                if (destination.getDimension().equals(minecraft.level.dimension())) {
                    int distance = (int) Math.sqrt(pos.distSqr(minecraft.player.blockPosition()));
                    description = Component.literal(coordinates + " (" + distance + " blocks)");
                } else {
                    description = Component.literal(coordinates + " (dim " + destination.getDimension().identifier() + ")");
                }
                tooltip.add(hint("Click: ", "to set this destination as current"));
                if (selectedBlock != null) {
                    tooltip.add(hint("Right-click: ", "to create portals to this destination"));
                }
                tooltip.add(hint("Del: ", "to remove this destination"));
            }
            graphics.centeredText(font, description, left() + SIZE / 2, top() + SIZE + 5, 0xffffffff);
            graphics.setTooltipForNextFrame(font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private static Component hint(String key, String action) {
        return Component.literal(key).withStyle(ChatFormatting.BLUE)
                .append(Component.literal(action).withStyle(ChatFormatting.WHITE));
    }
}
