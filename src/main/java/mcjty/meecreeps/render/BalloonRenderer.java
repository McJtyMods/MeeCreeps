package mcjty.meecreeps.render;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.gui.MeeCreepsDialogBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;

@EventBusSubscriber(modid = MeeCreeps.MODID, value = Dist.CLIENT)
public final class BalloonRenderer {
    private record Message(int expires, FormattedCharSequence text) {
    }

    private static final List<Message> messages = new ArrayList<>();
    private static int ticks;
    private static String lastMessage = "";

    public static void addMessage(String text) {
        lastMessage = text;
        var mc = Minecraft.getInstance();
        for (var line : mc.font.split(Component.literal(text), 230))
            messages.add(new Message(ticks + ConfigSetup.messageTimeout.get(), line));
    }

    public static void repeatLast() {
        if (!lastMessage.isEmpty())
            addMessage(lastMessage);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post e) {
        ticks++;
        messages.removeIf(m -> m.expires <= ticks);
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post e) {
        if (messages.isEmpty())
            return;
        GuiGraphicsExtractor g = e.getGuiGraphics();
        int w = g.guiWidth(), h = g.guiHeight(), boxW = MeeCreepsDialogBackground.WIDTH;
        int bodyRows = messages.size() - 1, boxH = MeeCreepsDialogBackground.height(bodyRows);
        int px = ConfigSetup.messageX.get(), py = ConfigSetup.messageY.get();
        int x = px == 0 ? (w - boxW) / 2 : px > 0 ? w * px / 100 : w + w * px / 100 - boxW;
        int y = py == 0 ? (h - boxH) / 2 : py > 0 ? h * py / 100 : h + h * py / 100 - boxH;
        MeeCreepsDialogBackground.render(g, x, y, bodyRows);
        for (var m : messages) {
            g.text(Minecraft.getInstance().font, m.text, x + 15, y + 7, 0xff000000, false);
            y += MeeCreepsDialogBackground.ROW_HEIGHT;
        }
    }
}
