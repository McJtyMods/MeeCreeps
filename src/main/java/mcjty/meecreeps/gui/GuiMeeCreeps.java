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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiMeeCreeps extends Screen {

    private static final int WIDTH = MeeCreepsDialogBackground.WIDTH;
    private static final int HEIGHT = 200;

    private static final int OPTION_DISTANCE = MeeCreepsDialogBackground.ROW_HEIGHT;
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
        super(net.minecraft.network.chat.Component.literal("MeeCreeps"));
        this.id = id;
    }

    @Override
    public void init() {
        super.init();
        guiLeft = (this.width - WIDTH) / 2;
        guiTop = (this.height - HEIGHT) / 2;

        options = ClientActionManager.lastOptions;
        confirmedAction = false;
        showingAlternatives = false;
        outsideWindowAction = () -> {
        };
    }

    @Override
    public boolean isPauseScreen() {
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
        this.minecraft.setScreen(null);
        if (this.minecraft.screen == null) {
            this.minecraft.mouseHandler.grabMouse();
        }
    }

    @Override
    public void removed() {
        if (!confirmedAction) {
            outsideWindowAction.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= guiLeft && mouseX <= guiLeft + WIDTH) {
            getQuestions();
            int row = (int) Math.floor((mouseY - guiTop - 21) / OPTION_DISTANCE);
            if (row >= 0 && row < questions.size())
                questions.get(row).getAction().run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void doAction(MeeCreepActionType type, MeeCreepsApi.Factory factory, String furtherQuestionId) {
        String heading = factory.getFactory().getFurtherQuestionHeading(Minecraft.getInstance().level, options.getTargetPos(), options.getTargetSide());
        if (heading == null || furtherQuestionId != null) {
            confirmedAction = true;
            MeeCreepsMessages.INSTANCE.sendToServer(new PacketPerformAction(options, type, furtherQuestionId));
            close();
        } else {
            furtherQuestionType = type;
            furtherQuestionsHeading = heading;
            furtherQuestions = factory.getFactory().getFurtherQuestions(Minecraft.getInstance().level, options.getTargetPos(), options.getTargetSide());
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
                questions.add(new Question("message.meecreeps.gui.other_things", () -> {
                    showingAlternatives = true;
                }));
            }
            questions.add(new Question("message.meecreeps.gui.never_mind", this::dismissAndClose));
            outsideWindowAction = this::dismiss;
        }
        return questions;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var rows = getQuestions();
        MeeCreepsDialogBackground.render(graphics, guiLeft, guiTop, rows.size());
        String heading = id == GuiProxy.GUI_MEECREEP_DISMISS ? "message.meecreeps.gui.problem"
                : furtherQuestionsHeading != null ? furtherQuestionsHeading
                : showingAlternatives ? "message.meecreeps.gui.alternatives"
                : "message.meecreeps.gui.what_can_i_do";
        graphics.drawString(font, I18n.get(heading), guiLeft + 15, guiTop + 7, 0xff000000, false);
        int y = guiTop + 21;
        for (var question : rows) {
            int color = mouseX >= guiLeft && mouseX < guiLeft + WIDTH && mouseY >= y && mouseY < y + OPTION_DISTANCE
                    ? 0xff22dd00 : 0xff666600;
            graphics.drawString(font, I18n.get(question.getMsg()), guiLeft + 40, y, color, false);
            y += OPTION_DISTANCE;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private boolean hasAlternatives() {
        return !options.getMaybeActionOptions().isEmpty();
    }
}
