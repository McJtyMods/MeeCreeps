package mcjty.meecreeps.items;

import com.google.gson.JsonObject;
import mcjty.meecreeps.MeeCreeps;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper.ShapedPrimer;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class InsertCartridgeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        ShapedOreRecipe recipe = ShapedOreRecipe.factory(context, json);

        ShapedPrimer primer = new ShapedPrimer();
        primer.width = recipe.getWidth();
        primer.height = recipe.getHeight();
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = recipe.getIngredients();

        return new InsertCartridgeRecipe(new ResourceLocation(MeeCreeps.MODID, "insert_cartridge_factory"), recipe.getRecipeOutput(), primer);
    }

    public static class InsertCartridgeRecipe extends ShapedOreRecipe {
        public InsertCartridgeRecipe(ResourceLocation group, ItemStack result, ShapedPrimer primer) {
            super(group, result, primer);
        }

        /**
         * Returns an Item that is the result of this recipe
         */
        @Override
        @Nonnull
        public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
            ItemStack newOutput = this.output.copy();

            ItemStack cartridge = ItemStack.EMPTY;
            ItemStack portalgun = ItemStack.EMPTY;

            for (int i = 0; i < var1.getSizeInventory(); ++i) {
                ItemStack stack = var1.getStackInSlot(i);

                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof CartridgeItem) {
                        cartridge = stack;
                    } else if (stack.getItem() instanceof EmptyPortalGunItem) {
                        portalgun = stack;
                    }
                }
            }

            if (portalgun.hasTagCompound()) {
                newOutput.setTagCompound(portalgun.getTagCompound().copy());
            }
            if (!cartridge.isEmpty()) {
                int charge = CartridgeItem.getCharge(cartridge);
                PortalGunItem.setCharge(newOutput, charge);
            }

            return newOutput;
        }
    }
}