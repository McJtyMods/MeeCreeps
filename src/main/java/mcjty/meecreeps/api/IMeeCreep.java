package mcjty.meecreeps.api;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Predicate;

public interface IMeeCreep {

    Entity getEntity();

    World getWorld();

    Random getRNG();

    // Add an itemstack to the internal inventory and return what could not be added
    ItemStack addStack(ItemStack stack);

    NonNullList<ItemStack> getInventory();

    Predicate<ItemStack> getInventoryMatcher();

    boolean hasEmptyInventory();

    boolean hasStuffInInventory();

    boolean hasItem(Predicate<ItemStack> matcher);

    void dropInventory();

    ItemStack consumeItem(Predicate<ItemStack> matcher, int amount);
}
