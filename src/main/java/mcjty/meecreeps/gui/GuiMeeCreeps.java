package mcjty.meecreeps.gui;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.proxy.GuiProxy;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiMeeCreeps extends GuiScreen {

    private static final int WIDTH = 256;
    private static final int HEIGHT = 200;

    private static final ResourceLocation gui_top = new ResourceLocation(MeeCreeps.MODID, "textures/gui/gui_meecreeps_top.png");
    private final int OPTION_DISTANCE = 14;
    private final int id;

    private int guiLeft;
    private int guiTop;

    private ActionOptions options;
    private boolean confirmedAction = false;
    private boolean showingAlternatives = false;
    private List<Question> questions = Collections.emptyList();
    private Runnable outsideWindowAction;

    public GuiMeeCreeps(int id) {
        this.id = id;
    }

    @Override
    public void initGui() {
        super.initGui();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;

        options = ClientActionManager.lastOptions;
        confirmedAction = false;
        showingAlternatives = false;
        outsideWindowAction = () -> {};
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void resumeAndClose() {
        resume();
        close();
    }

    private void resume() {
        confirmedAction = true;
        if (options != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketResumeAction(options));
        }
    }

    private void dismissAndClose() {
        dismiss();
        close();
    }

    private void dismiss() {
        confirmedAction = true;
        if (options != null) {
            PacketHandler.INSTANCE.sendToServer(new PacketCancelAction(options));
        }
    }

    private void close() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void onGuiClosed() {
        if (!confirmedAction) {
            outsideWindowAction.run();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int y = (mouseY - guiTop - 21) / OPTION_DISTANCE;
            if (y >= 0 && y < questions.size()) {
                questions.get(y).getAction().run();
            } else {
                close();
            }
        }
    }

    private void doAction(MeeCreepActionType type) {
        confirmedAction = true;
        PacketHandler.INSTANCE.sendToServer(new PacketPerformAction(options, type));
        close();
    }

    static class Question {
        private final String msg;
        private final Runnable action;

        public Question(String msg, Runnable action) {
            this.msg = msg;
            this.action = action;
        }

        public String getMsg() {
            return msg;
        }

        public Runnable getAction() {
            return action;
        }
    }

    private List<Question> getQuestions() {
        questions = new ArrayList<>();
        if (id == GuiProxy.GUI_MEECREEP_DISMISS) {
            questions.add(new Question("Please stop now!", this::dismissAndClose));
            questions.add(new Question("Carry on...", this::resumeAndClose));
            outsideWindowAction = this::resume;
        } else if (showingAlternatives) {
            List<MeeCreepActionType> opts = options.getMaybeActionOptions();
            for (MeeCreepActionType type : opts) {
                questions.add(new Question(type.getDescription(), () -> doAction(type)));
            }
            questions.add(new Question("Never mind...", this::close));
            outsideWindowAction = this::dismiss;
        } else {
            List<MeeCreepActionType> opts = options.getActionOptions();
            for (MeeCreepActionType type : opts) {
                questions.add(new Question(type.getDescription(), () -> doAction(type)));
            }
            if (hasAlternatives()) {
                questions.add(new Question("Can you do other things?", () -> { showingAlternatives = true;}));
            }
            questions.add(new Question("Never mind...", this::dismissAndClose));
            outsideWindowAction = this::dismiss;
        }
        return questions;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(gui_top);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        List<Question> questions = getQuestions();

        for (int i = 0; i < questions.size() ; i++) {
            drawTexturedModalRect(guiLeft, y, 0, 10, WIDTH, 15);
            y += OPTION_DISTANCE;
        }
        drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);
        String msg;

        if (id == GuiProxy.GUI_MEECREEP_DISMISS) {
            msg = "Is there a problem?";
        } else if (showingAlternatives) {
            msg = "Any of this suits you then?";
        } else if (options.getActionOptions().isEmpty()) {
            msg = "There is not much I can do here";
        } else {
            msg = "What can I do for you?";
        }
        mc.fontRenderer.drawString(msg, guiLeft+15, guiTop+7, 0);
        y = guiTop+21;
        for (Question question : questions) {
            int color = 0xff666600;
            if (mouseY > y && mouseY < y+OPTION_DISTANCE) {
                color = 0xff22dd00;
            }
            mc.fontRenderer.drawString(question.getMsg(), guiLeft+40, y, color);
            y += OPTION_DISTANCE;
        }
    }

    private boolean hasAlternatives() {
        return !options.getMaybeActionOptions().isEmpty();
    }
}
