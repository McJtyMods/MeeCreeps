package mcjty.meecreeps.items;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.level.Level;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

public class RemoveCartridgeFactory extends CustomRecipe {
    public RemoveCartridgeFactory(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    public boolean matches(CraftingContainer inv, Level world) {
        int guns = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
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

    public ItemStack assemble(CraftingContainer inv, RegistryAccess registries) {
        ItemStack output = new ItemStack(Registration.CARTRIDGE.get());
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var s = inv.getItem(i);
            if (s.is(Registration.GUN.get())) {
                CartridgeItem.setCharge(output, PortalGunItem.getCharge(s));
                if (s.hasTag())
                    output.getOrCreateTag().putInt("energyRemainder", s.getTag().getInt("energyRemainder"));
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
