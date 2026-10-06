package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.network.PacketServerCommand;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityProjectile;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;

import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PortalGunItem extends Item {

    public PortalGunItem() {
        super(new Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "portalgun"))).stacksTo(1));
    }

    public static ItemStack getGun(Player player) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() != Registration.GUN.get()) {
            heldItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (heldItem.getItem() != Registration.GUN.get()) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<net.minecraft.network.chat.Component> tooltip, TooltipFlag flagIn) {
        for (String line : StringUtils.split(I18n.get("message.meecreeps.tooltip.portalgun", Integer.toString(getCharge(stack))), "\n"))
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
            if (world.getBlockState(pos.relative(side)).getBlock() == Registration.PORTAL.get()) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketServerCommand(CommandHandler.CMD_CANCEL_PORTAL, pos.relative(side)));
                return InteractionResult.SUCCESS;
            }
            if (side != Direction.UP && side != Direction.DOWN && world.getBlockState(pos.relative(side).below()).getBlock() == Registration.PORTAL.get()) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketServerCommand(CommandHandler.CMD_CANCEL_PORTAL, pos.relative(side).below()));
                return InteractionResult.SUCCESS;
            }

            if (player.isShiftKeyDown()) {
                mcjty.meecreeps.setup.ClientSetup.openWheel(pos, side);
            }
            return InteractionResult.SUCCESS;
        } else {
            if (world.getBlockState(pos.relative(side)).getBlock() == Registration.PORTAL.get()) {
                return InteractionResult.SUCCESS;
            }
            if (side != Direction.UP && side != Direction.DOWN && world.getBlockState(pos.relative(side).below()).getBlock() == Registration.PORTAL.get()) {
                return InteractionResult.SUCCESS;
            }

            if (!player.isShiftKeyDown()) {
                throwProjectile(player, hand, world);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private void throwProjectile(Player player, InteractionHand hand, Level world) {
        ItemStack heldItem = player.getItemInHand(hand);

        int charge = getCharge(heldItem);
        if (charge <= 0) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_charge"), (ServerPlayer) player);
            return;
        }

        List<TeleportDestination> destinations = getDestinations(heldItem);
        int current = getCurrentDestination(heldItem);
        if (current < 0 || current >= destinations.size()) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_destination"), (ServerPlayer) player);
        } else if (destinations.get(current) == null) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_bad_destination"), (ServerPlayer) player);
        } else {
            EntityProjectile projectile = new EntityProjectile(world, player);
            projectile.setDestination(destinations.get(current));
            projectile.setPlayerId(player.getUUID());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            if (world.addFreshEntity(projectile))
                setCharge(heldItem, charge - 1);
        }
    }

    public static void addDestination(ItemStack stack, @Nullable TeleportDestination destination, int destinationIndex) {
        if (destinationIndex < 0 || destinationIndex >= 8)
            return;
        List<TeleportDestination> destinations = getDestinations(stack);
        destinations.set(destinationIndex, destination);
        setDestinations(stack, destinations);
        if (destination != null) {
            setCurrentDestination(stack, destinationIndex);
        }
    }

    private static void setDestinations(ItemStack stack, List<TeleportDestination> destinations) {
        ListTag dests = new ListTag();
        for (TeleportDestination destination : destinations) {
            if (destination != null) {
                dests.add(destination.getCompound());
            } else {
                dests.add(new CompoundTag());
            }
        }
        StackData.update(stack, data -> data.put("dests", dests));
    }

    public static int getCurrentDestination(ItemStack stack) {
        CompoundTag tag = StackData.get(stack);
        if (tag == null || !tag.contains("destination")) {
            return -1;
        }
        return tag.getIntOr("destination", 0);
    }

    public static void setCurrentDestination(ItemStack stack, int dest) {
        if (dest < -1 || dest >= 8)
            return;
        StackData.update(stack, data -> data.putInt("destination", dest));
    }

    public static List<TeleportDestination> getDestinations(ItemStack stack) {
        List<TeleportDestination> destinations = new ArrayList<>();
        if (!StackData.has(stack)) {
            for (int i = 0; i < 8; i++) {
                destinations.add(null);
            }
        } else {
            CompoundTag tag = StackData.get(stack);
            ListTag dests = tag.getListOrEmpty("dests");
            for (int i = 0; i < 8; i++) {
                CompoundTag tc = i < dests.size() ? dests.getCompoundOrEmpty(i) : null;
                if (tc != null && tc.contains("dim")) {
                    destinations.add(new TeleportDestination(tc));
                } else {
                    destinations.add(null);
                }
            }
        }
        return destinations;
    }

    public static void setCharge(ItemStack stack, int charge) {
        ItemEnergy.setCharge(stack, charge);
    }

    public static int getCharge(ItemStack stack) {
        if (!StackData.has(stack)) {
            return 0;
        }
        return StackData.get(stack).getIntOr("charge", 0);
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

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack stack = new ItemStack(Registration.EMPTY_GUN.get());
        if (StackData.has(itemStack)) {
            CompoundTag data = StackData.get(itemStack);
            data.remove("charge");
            data.remove("energyRemainder");
            StackData.set(stack, data);
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide()) {
            if (!player.isShiftKeyDown()) {
                throwProjectile(player, hand, world);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
