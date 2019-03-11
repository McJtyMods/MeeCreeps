package mcjty.meecreeps.gui;

import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ClientActionManager;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.actions.PacketPerformAction;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.GuiProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

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
    private String furtherQuestionsHeading = null;
    private List<Pair<String, String>> furtherQuestions = Collections.emptyList();
    private MeeCreepActionType furtherQuestionType;

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
            MeeCreepsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_RESUME_ACTION,
                    TypedMap.builder().put(CommandHandler.PARAM_ID, options.getActionId()).build()));
        }
    }

    private void dismissAndClose() {
        dismiss();
        close();
    }

    private void dismiss() {
        confirmedAction = true;
        if (options != null) {
            MeeCreepsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_ACTION,
                    TypedMap.builder().put(CommandHandler.PARAM_ID, options.getActionId()).build()));
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

    private void doAction(MeeCreepActionType type, MeeCreepsApi.Factory factory, String furtherQuestionId) {
        String heading = factory.getFactory().getFurtherQuestionHeading(Minecraft.getMinecraft().world, options.getTargetPos(), options.getTargetSide());
        if (heading == null || furtherQuestionId != null) {
            confirmedAction = true;
            MeeCreepsMessages.INSTANCE.sendToServer(new PacketPerformAction(options, type, furtherQuestionId));
            close();
        } else {
            furtherQuestionType = type;
            furtherQuestionsHeading = heading;
            furtherQuestions = factory.getFactory().getFurtherQuestions(Minecraft.getMinecraft().world, options.getTargetPos(), options.getTargetSide());
        }
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
            questions.add(new Question("message.meecreeps.gui.please_stop_now", this::dismissAndClose));
            questions.add(new Question("message.meecreeps.gui.carry_on", this::resumeAndClose));
            outsideWindowAction = this::resume;
        } else if (furtherQuestionsHeading != null) {
            MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(furtherQuestionType);
            for (Pair<String, String> pair : furtherQuestions) {
                questions.add(new Question(pair.getRight(), () -> doAction(furtherQuestionType, factory, pair.getLeft())));
            }
            questions.add(new Question("message.meecreeps.gui.never_mind", this::close));
            outsideWindowAction = this::dismiss;
        } else if (showingAlternatives) {
            List<MeeCreepActionType> opts = options.getMaybeActionOptions();
            for (MeeCreepActionType type : opts) {
                MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(type);
                questions.add(new Question(factory.getMessage(), () -> doAction(type, factory, null)));
            }
            questions.add(new Question("message.meecreeps.gui.never_mind", this::close));
            outsideWindowAction = this::dismiss;
        } else {
            List<MeeCreepActionType> opts = options.getActionOptions();
            for (MeeCreepActionType type : opts) {
                MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(type);
                questions.add(new Question(factory.getMessage(), () -> doAction(type, factory, null)));
            }
            if (hasAlternatives()) {
                questions.add(new Question("message.meecreeps.gui.other_things", () -> { showingAlternatives = true;}));
            }
            questions.add(new Question("message.meecreeps.gui.never_mind", this::dismissAndClose));
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
            msg = "message.meecreeps.gui.problem";
        } else if (furtherQuestionsHeading != null) {
            msg = furtherQuestionsHeading;
        } else if (showingAlternatives) {
            msg = "message.meecreeps.gui.alternatives";
        } else if (options.getActionOptions().isEmpty()) {
            msg = "message.meecreeps.gui.cant_do_much";
        } else {
            msg = "message.meecreeps.gui.what_can_i_do";
        }
        mc.fontRenderer.drawString(I18n.format(msg), guiLeft+15, guiTop+7, 0);
        y = guiTop+21;
        for (Question question : questions) {
            int color = 0xff666600;
            if (mouseY > y && mouseY < y+OPTION_DISTANCE) {
                color = 0xff22dd00;
            }
            mc.fontRenderer.drawString(I18n.format(question.getMsg()), guiLeft+40, y, color);
            y += OPTION_DISTANCE;
        }
    }

    private boolean hasAlternatives() {
        return !options.getMaybeActionOptions().isEmpty();
    }
}
