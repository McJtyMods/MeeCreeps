package mcjty.meecreeps.api;

import net.minecraft.entity.EntityCreature;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Predicate;

/**
 * The MeeCreep
 */
public interface IMeeCreep {

    EntityCreature getEntity();

    World getWorld();

    Random getRandom();

    /**
     * Add an itemstack to the internal inventory and return what could not be added
     */
    ItemStack addStack(ItemStack stack);

    /**
     * Get the current inventory
     */
    NonNullList<ItemStack> getInventory();

    /**
     * Get a predicate that returns true on itemstacks that are also in the inventory
     * of the MeeCreep
     */
    Predicate<ItemStack> getInventoryMatcher();

    /**
     * Return true if the inventory is empty
     */
    boolean hasEmptyInventory();

    /**
     * Return true if the inventory is not empty
     */
    boolean hasStuffInInventory();

    /**
     * Return true if the MeeCreep has an item matching the predicate
     */
    boolean hasItem(Predicate<ItemStack> matcher);

    /**
     * Return true if the MeeCreep has at least the given number of items matching the predicate
     */
    boolean hasItems(Predicate<ItemStack> matcher, int amount);

    /**
     * Return true if the MeeCreep has room for an item matching the predicate
     */
    boolean hasRoom(Predicate<ItemStack> matcher);

    /**
     * Let the MeeCreep drop everything he holds
     */
    void dropInventory();

    /**
     * Remove the first itemstack from the inventory that matches the predicate and return at
     * most 'amount' of it
     */
    ItemStack consumeItem(Predicate<ItemStack> matcher, int amount);
}
