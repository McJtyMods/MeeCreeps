package mcjty.meecreeps.gui;

import mcjty.lib.client.RenderHelper;
import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ClientActionManager;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketPerformAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiMeeCreeps extends Screen {

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
        super(new StringTextComponent(""));
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
        outsideWindowAction = () -> {};
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
            PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_RESUME_ACTION,
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
            PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_ACTION,
                    TypedMap.builder().put(CommandHandler.PARAM_ID, options.getActionId()).build()));
        }
    }

    private void close() {
        getMinecraft().displayGuiScreen(null);
        if (getMinecraft().currentScreen == null) {
            getMinecraft().setGameFocused(true);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!confirmedAction) {
            outsideWindowAction.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int y = (int) ((mouseY - guiTop - 21) / OPTION_DISTANCE);
            if (y >= 0 && y < questions.size()) {
                questions.get(y).getAction().run();
            } else {
                close();
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void doAction(MeeCreepActionType type, MeeCreepsApi.Factory factory, String furtherQuestionId) {
        String heading = factory.getFactory().getFurtherQuestionHeading(Minecraft.getInstance().world, options.getTargetPos(), options.getTargetSide());
        if (heading == null || furtherQuestionId != null) {
            confirmedAction = true;
            PacketHandler.INSTANCE.sendToServer(new PacketPerformAction(options.getActionId(), type, furtherQuestionId));
            close();
        } else {
            furtherQuestionType = type;
            furtherQuestionsHeading = heading;
            furtherQuestions = factory.getFactory().getFurtherQuestions(Minecraft.getInstance().world, options.getTargetPos(), options.getTargetSide());
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
        if (id == 2) { // 2 == GUI_MEECREEP_DISMISS todo: fix this
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        getMinecraft().getTextureManager().bindTexture(gui_top);
        RenderHelper.drawTexturedModalRect(guiLeft, guiTop, 0, 0, WIDTH, 10);
        int y = guiTop+10;

        List<Question> questions = getQuestions();

        for (int i = 0; i < questions.size() ; i++) {
            RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 10, WIDTH, 15);
            y += OPTION_DISTANCE;
        }
        RenderHelper.drawTexturedModalRect(guiLeft, y, 0, 25, WIDTH, 15);
        String msg;

        if (id == 2) { // 2 = GUI_MEECREEP_DISMISS todo: fix this
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
        getMinecraft().fontRenderer.drawString(I18n.format(msg), guiLeft+15, guiTop+7, 0);
        y = guiTop+21;
        for (Question question : questions) {
            int color = 0xff666600;
            if (mouseY > y && mouseY < y+OPTION_DISTANCE) {
                color = 0xff22dd00;
            }
            getMinecraft().fontRenderer.drawString(I18n.format(question.getMsg()), guiLeft+40, y, color);
            y += OPTION_DISTANCE;
        }
    }

    private boolean hasAlternatives() {
        return !options.getMaybeActionOptions().isEmpty();
    }
}
