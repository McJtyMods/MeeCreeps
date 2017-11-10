package mcjty.meecreeps.api;

import net.minecraft.util.math.BlockPos;

/**
 * Implement this interface for a build schematic using IDesiredBlock
 */
public interface IBuildSchematic {

    /**
     * Return the minimum coordinate of the schematic (given in coordinates relative to the target
     * position)
     */
    BlockPos getMinPos();

    /**
     * Return the minimum coordinate of the schematic (given in coordinates relative to the target
     * position)
     */
    BlockPos getMaxPos();

    /**
     * Return the desired block relative to the target position (where the MeeCreep was spawned)
     * Note that this is the block that was selected for targetting. Usually this means a block
     * in the ground so one lower then where the building is supposed to go unless you also want
     * to define a floor at that level.
     */
    IDesiredBlock getDesiredBlock(BlockPos relativePos);
}
