package mcjty.meecreeps.gui;

import mcjty.meecreeps.teleport.*;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiAskName extends Screen {
    private final int index;
    private final TeleportDestination destination;
    private EditBox name;

    public GuiAskName(int index, TeleportDestination destination) {
        super(Component.literal("Destination name"));
        this.index = index;
        this.destination = destination;
    }

    @Override
    protected void init() {
        name = new EditBox(font, width / 2 - 100, height / 2 - 30, 200, 20, title);
        name.setMaxLength(64);
        addRenderableWidget(name);
        setInitialFocus(name);
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
            String label = name.getValue().trim();
            if (!label.isEmpty()) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketSetDestination(new TeleportDestination(label, destination.getDimension(), destination.getPos(), destination.getSide()), index));
                onClose();
            }
        }).bounds(width / 2 - 50, height / 2, 100, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int x, int y, float partial) {
        super.render(g, x, y, partial);
        g.drawCenteredString(font, title, width / 2, height / 2 - 55, 0xffffffff);
    }
}
