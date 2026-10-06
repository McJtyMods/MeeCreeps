package mcjty.meecreeps.items;

import mcjty.meecreeps.actions.ClientActionManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class EmptyPortalGunItem extends Item implements FirstUseItem {

    public EmptyPortalGunItem() {
        super(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "emptyportalgun"))).stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, TooltipFlag flagIn) {
        for (String line : StringUtils.split(I18n.get("message.meecreeps.tooltip.emptyportalgun"), "\n"))
            tooltip.accept(net.minecraft.network.chat.Component.literal(line));
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
        if (world.isClientSide()) {
            ClientActionManager.showProblem("message.meecreeps.missing_cartridge");
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide()) {
//            BlockPos pos = player.blockPosition();
            ClientActionManager.showProblem("message.meecreeps.missing_cartridge");
        }
        return InteractionResult.SUCCESS;
    }
}
