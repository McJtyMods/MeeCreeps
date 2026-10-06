package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

public class InsertCartridgeFactory extends CustomRecipe {
    public InsertCartridgeFactory(CraftingBookCategory category) {
        super(category);
    }

    public boolean matches(CraftingInput inv, Level world) {
        int guns = 0, cartridges = 0;
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.isEmpty())
                continue;
            if (s.is(Registration.EMPTY_GUN.get()))
                guns++;
            else if (s.is(Registration.CARTRIDGE.get()))
                cartridges++;
            else
                return false;
        }
        return guns == 1 && cartridges == 1;
    }

    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack output = new ItemStack(Registration.GUN.get());
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.EMPTY_GUN.get()) && StackData.has(s))
                StackData.set(output, StackData.get(s).copy());
        }
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.CARTRIDGE.get())) {
                PortalGunItem.setCharge(output, CartridgeItem.getCharge(s));
                StackData.update(output, data -> data.putInt("energyRemainder", StackData.get(s).getInt("energyRemainder")));
            }
        }
        return output;
    }

    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return Registration.INSERT.get();
    }
}
