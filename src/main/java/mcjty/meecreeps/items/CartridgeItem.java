package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.gui.GuiBalloon;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.proxy.GuiProxy;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class CartridgeItem extends Item {

    public CartridgeItem() {
        setRegistryName("cartridge");
        setUnlocalizedName(MeeCreeps.MODID + ".cartridge");
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GREEN + "Cartridge for the portal gun");
        tooltip.add(TextFormatting.GREEN + "Right click to charge by consuming");
        tooltip.add(TextFormatting.GREEN + "ender pearls in inventory");
    }


    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
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
        int max = 32;
        int stored = getCharge(stack);
        return (max - stored) / (double) max;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote) {
            chargeCartridge(player, world, pos, hand);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            chargeCartridge(player, world, player.getPosition(), hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private void chargeCartridge(EntityPlayer player, World world, BlockPos pos, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        int charge = getCharge(heldItem);
        if (charge >= 29) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("This cartridge is full!"), (EntityPlayerMP) player);
        } else {
            for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack.getItem() == Items.ENDER_PEARL) {
                    ItemStack splitted = stack.splitStack(1);
                    charge += 4;
                    setCharge(heldItem, charge);
                    return;
                }
            }
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("No ender pearls!"), (EntityPlayerMP) player);
        }
    }

}
