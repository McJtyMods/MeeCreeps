package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

public class InsertCartridgeFactory extends CustomRecipe {
    public InsertCartridgeFactory(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public boolean matches(CraftingContainer inv, Level world) {
        int guns = 0, cartridges = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
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

    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        ItemStack output = new ItemStack(Registration.GUN.get());
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.EMPTY_GUN.get()) && s.hasTag())
                output.setTag(s.getTag().copy());
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.CARTRIDGE.get())) {
                PortalGunItem.setCharge(output, CartridgeItem.getCharge(s));
                if (s.hasTag())
                    output.getOrCreateTag().putInt("energyRemainder", s.getTag().getInt("energyRemainder"));
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
