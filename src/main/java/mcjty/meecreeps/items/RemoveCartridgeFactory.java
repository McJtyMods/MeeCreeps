package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

public class RemoveCartridgeFactory extends CustomRecipe {
    public RemoveCartridgeFactory() {
        super();
    }

    public boolean matches(CraftingInput inv, Level world) {
        int guns = 0;
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.isEmpty())
                continue;
            if (s.is(Registration.GUN.get()))
                guns++;
            else
                return false;
        }
        return guns == 1;
    }

    public ItemStack assemble(CraftingInput inv) {
        ItemStack output = new ItemStack(Registration.CARTRIDGE.get());
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.GUN.get())) {
                CartridgeItem.setCharge(output, PortalGunItem.getCharge(s));
                if (StackData.has(s))
                    StackData.update(output, data -> data.putInt("energyRemainder", StackData.get(s).getIntOr("energyRemainder", 0)));
            }
        }
        return output;
    }

    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        var result = net.minecraft.core.NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Registration.GUN.get()))
                result.set(i, ((PortalGunItem) Registration.GUN.get()).getCraftingRemainingItem(input.getItem(i)));
        }
        return result;
    }

    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 1;
    }

    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return Registration.REMOVE.get();
    }
}
