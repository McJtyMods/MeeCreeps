package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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

public class CreepCubeItem extends Item {

    public CreepCubeItem() {
        setRegistryName("creepcube");
        setUnlocalizedName(MeeCreeps.MODID + ".creepcube");
        setMaxStackSize(1);
        setCreativeTab(MeeCreeps.creativeTab);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
//        tooltip.add(TextFormatting.GREEN + "Sneak right click: " + TextFormatting.WHITE + "remember destination");
        tooltip.add(TextFormatting.GREEN + "Right click: " + TextFormatting.WHITE + "spawn MeeCreep");
        if (isLimited()) {
            tooltip.add(TextFormatting.GREEN + "Uses left: " + TextFormatting.YELLOW + (Config.meeCreepBoxMaxUsage - getUsages(stack)));
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return isLimited();
    }

    private boolean isLimited() {
        return Config.meeCreepBoxMaxUsage > 0;
    }

    public static void setUsages(ItemStack stack, int uses) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("uses", uses);
    }

    public static int getUsages(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            return 0;
        }
        return stack.getTagCompound().getInteger("uses");
    }


    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int max = Config.meeCreepBoxMaxUsage;
        int usages = getUsages(stack);
        return usages / (double) max;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        if (isLimited()) {
            ItemStack heldItem = player.getHeldItem(hand);
            if (getUsages(heldItem) >= Config.meeCreepBoxMaxUsage) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("This MeeCreep box has become unusable"), (EntityPlayerMP) player);
                return EnumActionResult.SUCCESS;
            }
            setUsages(heldItem, getUsages(heldItem)+1);
        }
        ServerActionManager.getManager().createActionOptions(world, pos, side, player);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
