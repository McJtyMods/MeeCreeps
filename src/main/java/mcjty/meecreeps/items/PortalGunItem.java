package mcjty.meecreeps.items;

import mcjty.lib.network.PacketSendServerCommand;
import mcjty.lib.typed.TypedMap;
import mcjty.meecreeps.CommandHandler;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.entities.EntityProjectile;
import mcjty.meecreeps.gui.GuiWheel;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.proxy.GuiProxy;
import mcjty.meecreeps.teleport.TeleportDestination;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortalGunItem extends Item {

    public PortalGunItem() {
        setRegistryName("portalgun");
        setUnlocalizedName(MeeCreeps.MODID + ".portalgun");
        setMaxStackSize(1);
        setCreativeTab(MeeCreeps.creativeTab);
    }

    public static ItemStack getGun(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.portalGunItem) {
            heldItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (heldItem.getItem() != ModItems.portalGunItem) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Collections.addAll(tooltip, StringUtils.split(I18n.format("message.meecreeps.tooltip.portalgun", Integer.toString(getCharge(stack))), "\n"));
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            if (world.getBlockState(pos.offset(side)).getBlock() == ModBlocks.portalBlock) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_PORTAL, TypedMap.builder().put(CommandHandler.PARAM_POS, pos.offset(side)).build()));
                return EnumActionResult.SUCCESS;
            }
            if (side != EnumFacing.UP && side != EnumFacing.DOWN && world.getBlockState(pos.offset(side).down()).getBlock() == ModBlocks.portalBlock) {
                MeeCreepsMessages.INSTANCE.sendToServer(new PacketSendServerCommand(MeeCreeps.MODID, CommandHandler.CMD_CANCEL_PORTAL, TypedMap.builder().put(CommandHandler.PARAM_POS, pos.offset(side).down()).build()));
                return EnumActionResult.SUCCESS;
            }

            if (player.isSneaking()) {
                GuiWheel.selectedBlock = pos;
                GuiWheel.selectedSide = side;
                player.openGui(MeeCreeps.instance, GuiProxy.GUI_WHEEL, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return EnumActionResult.SUCCESS;
        } else {
            if (world.getBlockState(pos.offset(side)).getBlock() == ModBlocks.portalBlock) {
                return EnumActionResult.SUCCESS;
            }
            if (side != EnumFacing.UP && side != EnumFacing.DOWN && world.getBlockState(pos.offset(side).down()).getBlock() == ModBlocks.portalBlock) {
                return EnumActionResult.SUCCESS;
            }

            if (!player.isSneaking()) {
                throwProjectile(player, hand, world);
            }
        }

        return EnumActionResult.SUCCESS;
    }

    private void throwProjectile(EntityPlayer player, EnumHand hand, World world) {
        ItemStack heldItem = player.getHeldItem(hand);

        int charge = getCharge(heldItem);
        if (charge <= 0) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_charge"), (EntityPlayerMP) player);
            return;
        }
        setCharge(heldItem, charge-1);

        List<TeleportDestination> destinations = getDestinations(heldItem);
        int current = getCurrentDestination(heldItem);
        if (current == -1) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_no_destination"), (EntityPlayerMP) player);
        } else if (destinations.get(current) == null) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.gun_bad_destination"), (EntityPlayerMP) player);
        } else {
            EntityProjectile projectile = new EntityProjectile(world, player);
            projectile.setDestination(destinations.get(current));
            projectile.setPlayerId(player.getUniqueID());
            projectile.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(projectile);
        }
    }

    public static void addDestination(ItemStack stack, @Nullable TeleportDestination destination, int destinationIndex) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        List<TeleportDestination> destinations = getDestinations(stack);
        destinations.set(destinationIndex, destination);
        setDestinations(stack, destinations);
        if (destination != null) {
            setCurrentDestination(stack, destinationIndex);
        }
    }

    private static void setDestinations(ItemStack stack, List<TeleportDestination> destinations) {
        NBTTagList dests = new NBTTagList();
        for (TeleportDestination destination : destinations) {
            if (destination != null) {
                dests.appendTag(destination.getCompound());
            } else {
                dests.appendTag(new NBTTagCompound());
            }
        }
        stack.getTagCompound().setTag("dests", dests);
    }

    public static int getCurrentDestination(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return -1;
        }
        return tag.getInteger("destination");
    }

    public static void setCurrentDestination(ItemStack stack, int dest) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("destination", dest);
    }

    public static List<TeleportDestination> getDestinations(ItemStack stack) {
        List<TeleportDestination> destinations = new ArrayList<>();
        if (!stack.hasTagCompound()) {
            for (int i = 0; i < 8; i++) {
                destinations.add(null);
            }
        } else {
            NBTTagCompound tag = stack.getTagCompound();
            NBTTagList dests = tag.getTagList("dests", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < 8; i++) {
                NBTTagCompound tc = i < dests.tagCount() ? dests.getCompoundTagAt(i) : null;
                if (tc != null && tc.hasKey("dim")) {
                    destinations.add(new TeleportDestination(tc));
                } else {
                    destinations.add(null);
                }
            }
        }
        return destinations;
    }

    public static void setCharge(ItemStack stack, int charge) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("charge", charge);
    }

    public static int getCharge(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return 0;
        }
        return stack.getTagCompound().getInteger("charge");
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int max = Config.maxCharge;
        int stored = getCharge(stack);
        return (max - stored) / (double) max;
    }



    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public Item getContainerItem() {
        return ModItems.emptyPortalGunItem;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack stack = new ItemStack(ModItems.emptyPortalGunItem);
        stack.setTagCompound(itemStack.getTagCompound());
        return stack;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            if (!player.isSneaking()) {
                throwProjectile(player, hand, world);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
