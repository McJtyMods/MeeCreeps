package mcjty.meecreeps.items;

import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityProjectile;
import mcjty.meecreeps.gui.GuiWheel;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketShowBalloonToClient;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PortalGunItem extends Item {
    public PortalGunItem() {
        super(new Properties().maxStackSize(1).group(MeeCreeps.TAB));
//        setRegistryName("portalgun");
//        setUnlocalizedName(MeeCreeps.MODID + ".portalgun");
//        setMaxStackSize(1);
//        setCreativeTab(MeeCreeps.setup.getTab());
    }

    public static ItemStack getGun(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.PORTAL_GUN_ITEM.get()) {
            heldItem = player.getHeldItem(Hand.OFF_HAND);
            if (heldItem.getItem() != ModItems.PORTAL_GUN_ITEM.get()) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.addAll(GeneralTools.createListByNewLine("message.meecreeps.tooltip.portalgun", Integer.toString(getCharge(stack))));
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote) {
            if (context.getWorld().getBlockState(context.getPos().offset(context.getFace())).getBlock() == ModBlocks.PORTAL_BLOCK.get()) {
                PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_PORTAL, TypedMap.builder().put(CommandHandler.PARAM_POS, context.getPos().offset(context.getFace())).build()));
                return ActionResultType.SUCCESS;
            }
            if (context.getFace() != Direction.UP && context.getFace() != Direction.DOWN && context.getWorld().getBlockState(context.getPos().offset(context.getFace()).down()).getBlock() == ModBlocks.PORTAL_BLOCK.get()) {
                PacketHandler.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_PORTAL, TypedMap.builder().put(CommandHandler.PARAM_POS, context.getPos().offset(context.getFace()).down()).build()));
                return ActionResultType.SUCCESS;
            }

            if (context.getPlayer().isSneaking()) {
                GuiWheel.selectedBlock = context.getPos();
                GuiWheel.selectedSide = context.getFace();
// todo: re-add
//                context.getPlayer().openGui(MeeCreeps.instance, GuiProxy.GUI_WHEEL, context.getWorld(), context.getPos().getX(), context.getPos().getY(), context.getPos().getZ());
            }
            return ActionResultType.SUCCESS;
        } else {
            if (context.getWorld().getBlockState(context.getPos().offset(context.getFace())).getBlock() == ModBlocks.PORTAL_BLOCK.get()) {
                return ActionResultType.SUCCESS;
            }
            if (context.getFace() != Direction.UP && context.getFace() != Direction.DOWN && context.getWorld().getBlockState(context.getPos().offset(context.getFace()).down()).getBlock() == ModBlocks.PORTAL_BLOCK.get()) {
                return ActionResultType.SUCCESS;
            }

            if (!context.getPlayer().isSneaking()) {
                throwProjectile(context.getPlayer(), context.getHand(), context.getWorld());
            }
        }

        return ActionResultType.SUCCESS;
    }

    private void throwProjectile(PlayerEntity player, Hand hand, World world) {
        ItemStack heldItem = player.getHeldItem(hand);

        int charge = getCharge(heldItem);
        if (charge <= 0) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_charge"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            return;
        }
        setCharge(heldItem, charge-1);

        List<TeleportDestination> destinations = getDestinations(heldItem);
        int current = getCurrentDestination(heldItem);
        if (current == -1) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_destination"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        } else if (destinations.get(current) == null) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_bad_destination"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        } else {
            EntityProjectile projectile = new EntityProjectile(world, player);
            projectile.setDestination(destinations.get(current));
            projectile.setPlayerId(player.getUniqueID());
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.addEntity(projectile);
        }
    }

    public static void addDestination(ItemStack stack, @Nullable TeleportDestination destination, int destinationIndex) {
        List<TeleportDestination> destinations = getDestinations(stack);
        destinations.set(destinationIndex, destination);
        setDestinations(stack, destinations);
        if (destination != null) {
            setCurrentDestination(stack, destinationIndex);
        }
    }

    private static void setDestinations(ItemStack stack, List<TeleportDestination> destinations) {
        ListNBT dests = new ListNBT();
        for (TeleportDestination destination : destinations) {
            if (destination != null) {
                dests.add(destination.getCompound());
            } else {
                dests.add(new CompoundNBT());
            }
        }
        stack.getOrCreateTag().put("dests", dests);
    }

    public static int getCurrentDestination(ItemStack stack) {
        CompoundNBT tag = stack.getOrCreateTag();
        if (tag == null) {
            return -1;
        }
        return tag.getInt("destination");
    }

    public static void setCurrentDestination(ItemStack stack, int dest) {
        stack.getOrCreateTag().putInt("destination", dest);
    }

    public static List<TeleportDestination> getDestinations(ItemStack stack) {
        List<TeleportDestination> destinations = new ArrayList<>();
        if (!stack.hasTag()) {
            for (int i = 0; i < 8; i++) {
                destinations.add(null);
            }
        } else {
            CompoundNBT tag = stack.getOrCreateTag();
            ListNBT dests = tag.getList("dests", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < 8; i++) {
                CompoundNBT tc = i < dests.size() ? dests.getCompound(i) : null;
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
        stack.getOrCreateTag().putInt("charge", charge);
    }

    public static int getCharge(ItemStack stack) {
        if (!stack.getOrCreateTag().contains("charge")) {
            return 0;
        }

        return stack.getOrCreateTag().getInt("charge");
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int max = ConfigSetup.maxCharge.get();
        int stored = getCharge(stack);
        return (max - stored) / (double) max;
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack stack = new ItemStack(ModItems.EMPTY_PORTAL_GUN_ITEM.get());
        stack.setTag(itemStack.getOrCreateTag());
        return stack;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            if (!playerIn.isSneaking()) {
                throwProjectile(playerIn, handIn, worldIn);
            }
        }
        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

}
