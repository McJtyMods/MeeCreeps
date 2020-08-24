package mcjty.meecreeps.items;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ClientActionManager;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class EmptyPortalGunItem extends Item {

    public EmptyPortalGunItem() {
        super(new Properties().group(MeeCreeps.setup.getTab()).maxStackSize(1));
//        setRegistryName("emptyportalgun");
//        setUnlocalizedName(MeeCreeps.MODID + ".emptyportalgun");
//        setMaxStackSize(1);
//        setCreativeTab(MeeCreeps.setup.getTab());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.addAll(GeneralTools.createListByNewLine("message.meecreeps.tooltip.emptyportalgun"));
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote) {
//            GuiBalloon.message = "This gun does not have a cartridge!";
//            player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, world, pos.getX(), pos.getY(), pos.getZ());
            ClientActionManager.showProblem("message.meecreeps.missing_cartridge");
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.SUCCESS;
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (worldIn.isRemote) {
//            GuiBalloon.message = "This gun does not have a cartridge!";
//            BlockPos pos = player.getPosition();
//            player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_BALLOON, world, pos.getX(), pos.getY(), pos.getZ());
            ClientActionManager.showProblem("message.meecreeps.missing_cartridge");
        }

        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }
}
