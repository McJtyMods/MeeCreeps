package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.MeeCreepActionType;
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
        tooltip.add(TextFormatting.GREEN + "Right click: " + TextFormatting.WHITE + "spawn MeeCreep");
        tooltip.add(TextFormatting.GREEN + "Sneak right click: " + TextFormatting.WHITE + "repeat last action");
        MeeCreepActionType lastAction = getLastAction(stack);
        if (lastAction != null) {
            MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
            tooltip.add(TextFormatting.YELLOW + "    (" + factory.getMessage() + ")");
        }
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

    public static void setLastAction(ItemStack cube, MeeCreepActionType type, @Nullable String furtherQuestionId) {
        if (cube.getTagCompound() == null) {
            cube.setTagCompound(new NBTTagCompound());
        }
        cube.getTagCompound().setString("lastType", type.getId());
        if (furtherQuestionId != null) {
            cube.getTagCompound().setString("lastQuestion", furtherQuestionId);
        }
    }

    @Nullable
    public static MeeCreepActionType getLastAction(ItemStack cube) {
        if (cube.getTagCompound() == null) {
            return null;
        }
        if (!cube.getTagCompound().hasKey("lastType")) {
            return null;
        }
        String lastType = cube.getTagCompound().getString("lastType");
        return new MeeCreepActionType(lastType);
    }

    @Nullable
    public static String getLastQuestionId(ItemStack cube) {
        if (cube.getTagCompound() == null) {
            return null;
        }
        if (!cube.getTagCompound().hasKey("lastQuestion")) {
            return null;
        }
        return cube.getTagCompound().getString("lastQuestion");
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

    public static ItemStack getCube(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.getItem() != ModItems.creepCubeItem) {
            heldItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (heldItem.getItem() != ModItems.creepCubeItem) {
                // Something went wrong
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
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

        if (Config.maxMeecreepsPerPlayer >= 0) {
            int cnt = ServerActionManager.getManager().countMeeCreeps(player);
            if (cnt >= Config.maxMeecreepsPerPlayer) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("You can only spawn " + Config.maxMeecreepsPerPlayer + " MeeCreep(s) per player!"), (EntityPlayerMP) player);
                return EnumActionResult.SUCCESS;
            }
        }

        if (player.isSneaking()) {
            ItemStack heldItem = player.getHeldItem(hand);
            MeeCreepActionType lastAction = getLastAction(heldItem);
            if (lastAction == null) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("There is no last action!"), (EntityPlayerMP) player);
            } else {
                MeeCreepsApi.Factory factory = MeeCreeps.api.getFactory(lastAction);
                if (factory.getFactory().isPossible(world, pos, side) || factory.getFactory().isPossibleSecondary(world, pos, side)) {
                    MeeCreeps.api.spawnMeeCreep(lastAction.getId(), getLastQuestionId(heldItem), world, pos, side, (EntityPlayerMP) player, false);
                } else {
                    PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("The last action is not possible here!"), (EntityPlayerMP) player);
                }
            }
        } else {
            ServerActionManager.getManager().createActionOptions(world, pos, side, player);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return EnumActionResult.SUCCESS;
    }
}
