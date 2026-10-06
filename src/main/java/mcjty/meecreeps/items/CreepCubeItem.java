package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.List;

public class CreepCubeItem extends Item {

    public CreepCubeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flagIn) {
        for (String line : StringUtils.split(I18n.get("message.meecreeps.tooltip.cube_intro"), "\n"))
            tooltip.add(net.minecraft.network.chat.Component.literal(line));

        MeeCreepActionType lastAction = getLastAction(stack);
        if (lastAction != null && MeeCreeps.api.getFactory(lastAction) != null) {
            MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
            tooltip.add(net.minecraft.network.chat.Component.translatable(factory.getMessage()).withStyle(ChatFormatting.YELLOW));
        }
        if (isLimited()) {
            for (String line : StringUtils.split(I18n.get("message.meecreeps.tooltip.cube_uses", Integer.toString(ConfigSetup.meeCreepBoxMaxUsage.get() - getUsages(stack))), "\n"))
                tooltip.add(net.minecraft.network.chat.Component.literal(line));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isLimited();
    }

    private boolean isLimited() {
        return ConfigSetup.meeCreepBoxMaxUsage.get() > 0;
    }

    public static void setLastAction(ItemStack cube, MeeCreepActionType type, @Nullable String furtherQuestionId) {
        StackData.update(cube, data -> {
            data.putString("lastType", type.getId());
            if (furtherQuestionId != null)
                data.putString("lastQuestion", furtherQuestionId);
            else
                data.remove("lastQuestion");
        });
    }

    @Nullable
    public static MeeCreepActionType getLastAction(ItemStack cube) {
        if (!StackData.has(cube)) {
            return null;
        }
        if (!StackData.get(cube).contains("lastType")) {
            return null;
        }
        String lastType = StackData.get(cube).getString("lastType");
        return new MeeCreepActionType(lastType);
    }

    @Nullable
    public static String getLastQuestionId(ItemStack cube) {
        if (!StackData.has(cube)) {
            return null;
        }
        if (!StackData.get(cube).contains("lastQuestion")) {
            return null;
        }
        return StackData.get(cube).getString("lastQuestion");
    }

    public static void setUsages(ItemStack stack, int uses) {
        StackData.update(stack, data -> data.putInt("uses", uses));
    }

    public static int getUsages(ItemStack stack) {
        if (!StackData.has(stack)) {
            return 0;
        }
        return StackData.get(stack).getInt("uses");
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (1 - (float) getDurabilityForDisplay(stack)));
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        int max = ConfigSetup.meeCreepBoxMaxUsage.get();
        int usages = getUsages(stack);
        return usages / (double) max;
    }

    public static ItemStack getCube(Player player) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() != Registration.CUBE_ITEM.get()) {
            heldItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (heldItem.getItem() != Registration.CUBE_ITEM.get()) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack usedStack, net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        if (player == null)
            return InteractionResult.PASS;
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (isLimited()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (getUsages(heldItem) >= ConfigSetup.meeCreepBoxMaxUsage.get()) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.box_unusable"), (ServerPlayer) player);
                return InteractionResult.SUCCESS;
            }
        }

        if (ConfigSetup.maxMeecreepsPerPlayer.get() >= 0) {
            int cnt = ServerActionManager.getManager().countMeeCreeps(player);
            if (cnt >= ConfigSetup.maxMeecreepsPerPlayer.get()) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.max_spawn_reached", Integer.toString(ConfigSetup.maxMeecreepsPerPlayer.get())), (ServerPlayer) player);
                return InteractionResult.SUCCESS;
            }
        }

        if (player.isShiftKeyDown()) {
            ItemStack heldItem = player.getItemInHand(hand);
            MeeCreepActionType lastAction = getLastAction(heldItem);
            if (lastAction == null) {
                MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.no_last_action"), (ServerPlayer) player);
            } else {
                MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
                if (factory != null && ConfigSetup.isAllowed(lastAction.getId()) && (factory.getFactory().isPossible(world, pos, side) || factory.getFactory().isPossibleSecondary(world, pos, side))) {
                    if (isLimited())
                        setUsages(heldItem, getUsages(heldItem) + 1);
                    MeeCreeps.api.spawnMeeCreep(lastAction.getId(), getLastQuestionId(heldItem), world, pos, side, (ServerPlayer) player, false);
                } else {
                    MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.last_action_not_possible"), (ServerPlayer) player);
                }
            }
        } else {
            if (isLimited())
                setUsages(usedStack, getUsages(usedStack) + 1);
            ServerActionManager.getManager().createActionOptions(world, pos, side, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
}
