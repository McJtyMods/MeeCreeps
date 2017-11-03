package mcjty.meecreeps.api;

import mcjty.meecreeps.actions.Stage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

public interface IActionOptions {

    List<Pair<BlockPos, ItemStack>> getDrops();

    void clearDrops();

    /**
     * Get the position that was targetted when the MeeCreeps was spawned
     */
    BlockPos getTargetPos();

    /**
     * If that player is still online this will return the player that spawned the MeeCreeps. If not this returns null
     */
    @Nullable
    EntityPlayer getPlayer();

    int getActionId();

    Stage getStage();

    void setStage(Stage stage);

    boolean isPaused();

    void setPaused(boolean paused);
}
