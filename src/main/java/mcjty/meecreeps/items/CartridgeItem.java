package mcjty.meecreeps.items;

import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.MeeCreepsMessages;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class CartridgeItem extends Item {

    public CartridgeItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flagIn) {
        for (String line : StringUtils.split(I18n.get("message.meecreeps.tooltip.cartridge_item", Integer.toString(getCharge(stack))), "\n"))
            tooltip.add(net.minecraft.network.chat.Component.literal(line));
    }

    public static void setCharge(ItemStack stack, int charge) {
        ItemEnergy.setCharge(stack, charge);
    }

    public static int getCharge(ItemStack stack) {
        if (!StackData.has(stack)) {
            return 0;
        }
        return StackData.get(stack).getInt("charge");
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (1 - (float) getDurabilityForDisplay(stack)));
    }

    public double getDurabilityForDisplay(ItemStack stack) {
        int max = ConfigSetup.maxCharge.get();
        int stored = getCharge(stack);
        return (max - stored) / (double) max;
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
        if (!world.isClientSide) {
            chargeCartridge(player, world, pos, hand);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            chargeCartridge(player, world, player.blockPosition(), hand);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    private void chargeCartridge(Player player, Level world, BlockPos pos, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        int charge = getCharge(heldItem);
        if (charge >= (ConfigSetup.maxCharge.get() - ConfigSetup.chargesPerEnderpearl.get() + 1)) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cartridge_full"), (ServerPlayer) player);
        } else {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() == Items.ENDER_PEARL) {
                    ItemStack splitted = stack.split(1);
                    charge += ConfigSetup.chargesPerEnderpearl.get();
                    setCharge(heldItem, charge);
                    return;
                }
            }
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.missing_enderpearls"), (ServerPlayer) player);
        }
    }

}
