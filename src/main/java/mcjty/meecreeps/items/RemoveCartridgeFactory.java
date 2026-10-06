package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

public class RemoveCartridgeFactory extends CustomRecipe {
    public RemoveCartridgeFactory(CraftingBookCategory category) {
        super(category);
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

    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registries) {
        ItemStack output = new ItemStack(Registration.CARTRIDGE.get());
        for (int i = 0; i < inv.size(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.GUN.get())) {
                CartridgeItem.setCharge(output, PortalGunItem.getCharge(s));
                if (StackData.has(s))
                    StackData.update(output, data -> data.putInt("energyRemainder", StackData.get(s).getInt("energyRemainder")));
            }
        }
        return output;
    }

    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 1;
    }

    public RecipeSerializer<?> getSerializer() {
        return Registration.REMOVE.get();
    }
}
