package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketShowBalloonToClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreepCubeItem extends Item {

    public CreepCubeItem() {
        super(new Properties().maxStackSize(1).group(MeeCreeps.setup.getTab()));
//        setRegistryName("creepcube");
//        setUnlocalizedName(MeeCreeps.MODID + ".creepcube");
//        setMaxStackSize(1);
//        setCreativeTab(MeeCreeps.setup.getTab());
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.addAll(Arrays.stream(StringUtils.split(I18n.format("message.meecreeps.tooltip.cube_intro"), "\n")).map(StringTextComponent::new).collect(Collectors.toList()));

        MeeCreepActionType lastAction = getLastAction(stack);
        if (lastAction != null) {
            MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
            tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "    (" + I18n.format(factory.getMessage()) + ")"));
        }
        if (isLimited()) {
            tooltip.addAll(Arrays.stream(StringUtils.split(I18n.format("message.meecreeps.tooltip.cube_uses", Integer.toString(ConfigSetup.meeCreepBoxMaxUsage.get() - getUsages(stack))), "\n")).map(StringTextComponent::new).collect(Collectors.toList()));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return isLimited();
    }

    private boolean isLimited() {
        return ConfigSetup.meeCreepBoxMaxUsage.get() > 0;
    }

    public static void setLastAction(ItemStack cube, MeeCreepActionType type, @Nullable String furtherQuestionId) {
        cube.getOrCreateTag().putString("lastType", type.getId());
        if (furtherQuestionId != null) {
            cube.getOrCreateTag().putString("lastQuestion", furtherQuestionId);
        }
    }

    @Nullable
    public static MeeCreepActionType getLastAction(ItemStack cube) {
        if (!cube.getOrCreateTag().contains("lastType")) {
            return null;
        }
        String lastType = cube.getOrCreateTag().getString("lastType");
        return new MeeCreepActionType(lastType);
    }

    @Nullable
    public static String getLastQuestionId(ItemStack cube) {
        if (!cube.getOrCreateTag().contains("lastQuestion")) {
            return null;
        }
        return cube.getOrCreateTag().getString("lastQuestion");
    }

    public static void setUsages(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt("uses", uses);
    }

    public static int getUsages(ItemStack stack) {
        return stack.getOrCreateTag().getInt("uses");
    }


    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int max = ConfigSetup.meeCreepBoxMaxUsage.get();
        int usages = getUsages(stack);
        return usages / (double) max;
    }

    public static ItemStack getCube(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.creepCubeItem) {
            heldItem = player.getHeldItem(Hand.OFF_HAND);
            if (heldItem.getItem() != ModItems.creepCubeItem) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote || context.getPlayer() == null) {
            return ActionResultType.SUCCESS;
        }

        if (isLimited()) {
            ItemStack heldItem = context.getPlayer().getHeldItem(context.getHand());
            if (getUsages(heldItem) >= ConfigSetup.meeCreepBoxMaxUsage.get()) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.box_unusable"), ((ServerPlayerEntity) context.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return ActionResultType.SUCCESS;
            }
            setUsages(heldItem, getUsages(heldItem)+1);
        }

        if (ConfigSetup.maxMeecreepsPerPlayer.get() >= 0) {
            int cnt = ServerActionManager.getManager().countMeeCreeps(context.getPlayer());
            if (cnt >= ConfigSetup.maxMeecreepsPerPlayer.get()) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.max_spawn_reached", Integer.toString(ConfigSetup.maxMeecreepsPerPlayer.get())), ((ServerPlayerEntity) context.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return ActionResultType.SUCCESS;
            }
        }

        if (context.getPlayer().isSneaking()) {
            ItemStack heldItem = context.getPlayer().getHeldItem(context.getHand());
            MeeCreepActionType lastAction = getLastAction(heldItem);
            if (lastAction == null) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.no_last_action"), ((ServerPlayerEntity) context.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            } else {
                MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
                if (factory.getFactory().isPossible(context.getWorld(), context.getPos(), context.getFace()) || factory.getFactory().isPossibleSecondary(context.getWorld(), context.getPos(), context.getFace())) {
                    MeeCreeps.api.spawnMeeCreep(lastAction.getId(), getLastQuestionId(heldItem), context.getWorld(), context.getPos(), context.getFace(), (ServerPlayerEntity) context.getPlayer(), false);
                } else {
                    PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.last_action_not_possible"), ((ServerPlayerEntity) context.getPlayer()).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        } else {
            ServerActionManager.getManager().createActionOptions(context.getWorld(), context.getPos(), context.getFace(), context.getPlayer());
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.SUCCESS;
    }
}
