package mcjty.meecreeps.items;

import com.google.gson.JsonObject;
import mcjty.meecreeps.MeeCreeps;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RemoveCartridgeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new RemoveCartridgeRecipe(new ResourceLocation(MeeCreeps.MODID, "remove_cartridge_factory"), recipe.getRecipeOutput(), primer);
    }

    public static class RemoveCartridgeRecipe extends ShapedOreRecipe {
        public RemoveCartridgeRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }


        @Override
        public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting) {
            ItemStack result = super.getCraftingResult(inventoryCrafting);
            if (!result.isEmpty()) {

                ItemStack portalGunItem = ItemStack.EMPTY;

                for (int i = 0; i < inventoryCrafting.getSizeInventory(); ++i) {
                    ItemStack stack = inventoryCrafting.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        if (stack.getItem() instanceof PortalGunItem) {
                            portalGunItem = stack;
                        }
                    }
                }

                if (!portalGunItem.isEmpty()) {
                    int charge = PortalGunItem.getCharge(portalGunItem);
                    CartridgeItem.setCharge(result, charge);
                }
            }
            return result;
        }
    }
}