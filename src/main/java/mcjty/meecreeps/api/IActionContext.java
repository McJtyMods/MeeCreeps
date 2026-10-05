package mcjty.meecreeps.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

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
     * Get the side on the target position that was used to spawn this MeeCreep
     */
    Direction getTargetSide();

    /**
     * If the action required further questions then this will be the id of the selected
     * answer
     */
    @Nullable
    String getFurtherQuestionId();

    /**
     * If that player is still online this will return the player that spawned the MeeCreep. If not this returns null
     */
    @Nullable
    Player getPlayer();
}
