package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketShowBalloonToClient;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.List;

public class CartridgeItem extends Item {

    public CartridgeItem() {
        super(new Properties().maxStackSize(1).group(MeeCreeps.setup.getTab()));
//        setRegistryName("cartridge");
//        setUnlocalizedName(MeeCreeps.MODID + ".cartridge");
//        setMaxStackSize(1);
//        setCreativeTab(MeeCreeps.setup.getTab());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.addAll(GeneralTools.createListByNewLine("message.meecreeps.tooltip.cartridge_item", Integer.toString(getCharge(stack))));
    }


//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

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
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            chargeCartridge(context.getPlayer(), context.getWorld(), context.getPos(), context.getHand());
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            chargeCartridge(playerIn, worldIn, playerIn.getPosition(), handIn);
        }
        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

    private void chargeCartridge(PlayerEntity player, World world, BlockPos pos, Hand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        int charge = getCharge(heldItem);
        if (charge >= (ConfigSetup.maxCharge.get()- ConfigSetup.chargesPerEnderpearl.get()+1)) {
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cartridge_full"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        } else {
            for (int i = 0 ; i < player.inventory.getSizeInventory() ; i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack.getItem() == Items.ENDER_PEARL) {
                    ItemStack splitted = stack.split(1);
                    charge += ConfigSetup.chargesPerEnderpearl.get();
                    setCharge(heldItem, charge);
                    return;
                }
            }
            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.missing_enderpearls"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

}
