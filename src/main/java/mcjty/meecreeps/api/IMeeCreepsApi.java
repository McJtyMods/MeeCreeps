package mcjty.meecreeps.api;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Main interface for this mod. Use this to build probe information
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("meecreeps", "getMeeCreepsApi", "<whatever>.YourClass$GetMeeCreepsApi");
 */
public interface IMeeCreepsApi {

    /**
     * Register an action factory. The 'id' should preferably contain a modid. The message is shown
     * to the player.
     *
     * The standard actions are (with further question ids between brackets (if required)):
     *      * meecreeps.chop_tree
     *      * meecreeps.dig_down
     *      * meecreeps.dig_down_stairs (north, south, west, east)
     *      * meecreeps.make_house (9x9, 11x11, 13x13)
     *      * meecreeps.make_platform (9x9, 11x11, 13x13)
     *      * meecreeps.flatten_area (9x9, 11x11, 13x13)
     *      * meecreeps.mine_ores
     *      * meecreeps.dig_tunnel
     *      * meecreeps.chop_tree_collect
     *      * meecreeps.harvest_replant
     *      * meecreeps.harvest
     *      * meecreeps.torches
     *      * meecreeps.follow_and_lightup
     *      * meecreeps.follow_and_pickup
     *      * meecreeps.pickup
     *      * meecreeps.move_stuff
     *      * meecreeps.idle
     *      * meecreeps.angry
     */
    void registerActionFactory(String id, String message, IActionFactory factory);

    /**
     * Spawn a MeeCreep with the given action and target position. Optionally you can give
     * a player. If the player is not present then the MeeCreep will work as if there is no
     * player present.
     * The 'furtherQuestionId' is an optional id that some of the actions use for extra information
     * (this is normally asked from the player)
     *
     * Return false if the task was not possible for some reason (or invalid)
     */
    boolean spawnMeeCreep(String id, @Nullable String furtherQuestionId, World world, BlockPos targetPos, EnumFacing targetSide,
                          @Nullable EntityPlayerMP player, boolean doSound);
}
