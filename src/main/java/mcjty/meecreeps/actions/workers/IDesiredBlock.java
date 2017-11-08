package mcjty.meecreeps.actions.workers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public interface IDesiredBlock {

    default int getPass() { return 0; }

    default boolean isOptional() { return false; }

    /// Return the amount of items to get at max
    int getAmount();

    String getName();

    Predicate<ItemStack> getMatcher();

    Predicate<IBlockState> getStateMatcher();
}
