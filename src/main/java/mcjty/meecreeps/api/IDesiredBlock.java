package mcjty.meecreeps.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

/**
 * This interface indicates what kind of block one needs for building. Buildings are
 * built from the bottom layer to the top layer
 * See IBuildSchematic
 */
public interface IDesiredBlock {

    /**
     * A building can be done in two passes. Return 1 here for all blocks that have to be done later
     */
    default int getPass() { return 0; }

    /**
     * Return true if this block is optional and doesn't have to be built if the material is not there
     */
    default boolean isOptional() { return false; }

    /**
     * When fetching this item needed for building don't fetch more then this amount
     */
    int getAmount();

    /**
     * Get the name to tell the player when this block is missing
     */
    String getName();

    /**
     * This predicate should match all items that can be used to fulfill building of this block
     */
    Predicate<ItemStack> getMatcher();

    /**
     * This predicate should match all block states that correspond to this block in the world
     */
    Predicate<IBlockState> getStateMatcher();
}
