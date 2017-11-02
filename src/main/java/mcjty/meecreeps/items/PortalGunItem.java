package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.gui.GuiAskName;
import mcjty.meecreeps.gui.GuiBalloon;
import mcjty.meecreeps.gui.GuiWheel;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.proxy.GuiProxy;
import mcjty.meecreeps.teleport.PacketCancelPortal;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.teleport.TeleportationTools;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PortalGunItem extends Item {

    public PortalGunItem() {
        setRegistryName("portalgun");
        setUnlocalizedName(MeeCreeps.MODID + ".portalgun");
        setMaxStackSize(1);
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

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            if (!player.isSneaking()) {
                if (world.getBlockState(pos).getBlock() == ModBlocks.portalBlock) {
                    PacketHandler.INSTANCE.sendToServer(new PacketCancelPortal(pos));
                } else {
                    GuiWheel.selectedBlock = pos;
                    GuiWheel.selectedSide = side;
                    player.openGui(MeeCreeps.instance, GuiProxy.GUI_WHEEL, world, pos.getX(), pos.getY(), pos.getZ());
                }
            } else {
                BlockPos bestPosition = TeleportationTools.findBestPosition(world, pos, side);
                if (bestPosition == null) {
                    GuiBalloon.message = "Can't find a good spot to make a portal!";
                    player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, world, pos.getX(), pos.getY(), pos.getZ());
                } else {
                    GuiAskName.destination = new TeleportDestination("", world.provider.getDimension(), bestPosition, side);
                    player.openGui(MeeCreeps.instance, GuiProxy.GUI_ASKNAME, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return EnumActionResult.SUCCESS;
        } else {
//            addDestination(player.getHeldItem(hand), world, pos);
        }

        return EnumActionResult.SUCCESS;
    }

    public static void addDestination(ItemStack stack, World world, BlockPos pos, EnumFacing side, String name) {
        TeleportDestination destination = new TeleportDestination(name, world.provider.getDimension(), pos, side);
        addDestination(stack, destination);
    }

    public static void addDestination(ItemStack stack, TeleportDestination destination) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        List<TeleportDestination> destinations = getDestinations(stack);
        for (int i = 0 ; i < 8 ; i++) {
            if (destinations.get(i) == null) {
                destinations.set(i, destination);
                setDestinations(stack, destinations);
                // @todo give a note
                return;
            }
        }
        // @todo warning, no empty spot
    }

    private static void setDestinations(ItemStack stack, List<TeleportDestination> destinations) {
        NBTTagList dests = new NBTTagList();
        for (TeleportDestination destination : destinations) {
            NBTTagCompound tc = new NBTTagCompound();
            if (destination != null) {
                tc.setString("name", destination.getName());
                tc.setInteger("dim", destination.getDimension());
                tc.setByte("side", (byte) destination.getSide().ordinal());
                tc.setInteger("x", destination.getPos().getX());
                tc.setInteger("y", destination.getPos().getY());
                tc.setInteger("z", destination.getPos().getZ());
            }
            dests.appendTag(tc);
        }
        stack.getTagCompound().setTag("dests", dests);
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
                    String name = tc.getString("name");
                    int dim = tc.getInteger("dim");
                    BlockPos pos = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
                    EnumFacing side = EnumFacing.VALUES[tc.getByte("side")];
                    destinations.add(new TeleportDestination(name, dim, pos, side));
                } else {
                    destinations.add(null);
                }
            }
        }
        return destinations;
    }


    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        if (world.isRemote) {
            if (!player.isSneaking()) {
                BlockPos pos = player.getPosition();
                player.openGui(MeeCreeps.instance, GuiProxy.GUI_WHEEL, world, pos.getX(), pos.getY(), pos.getZ());
            }
        } else {
            // Add destination?
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(handIn));
    }
}
