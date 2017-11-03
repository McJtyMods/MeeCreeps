package mcjty.meecreeps.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * The context for an action. This represents the things that
 * the action works on or needs
 */
public interface IActionContext {

    /**
     * Get the position that was targetted when the MeeCreep was spawned
     */
    BlockPos getTargetPos();

    /**
     * If that player is still online this will return the player that spawned the MeeCreep. If not this returns null
     */
    @Nullable
    EntityPlayer getPlayer();
}
