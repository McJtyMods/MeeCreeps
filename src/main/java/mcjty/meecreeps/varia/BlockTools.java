package mcjty.meecreeps.varia;

import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;

import java.util.List;

public final class BlockTools {
    public static List<ItemStack> getDrops(Level world, BlockPos pos, BlockState state) {
        var player = GeneralTools.getHarvester(world);
        player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        return Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos), player, player.getMainHandItem());
    }

    public static BlockState placeStackAt(Player player, ItemStack stack, Level world, BlockPos pos, Direction side) {
        Direction face = side == null ? Direction.UP : side;
        player.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
        if (stack.getItem() instanceof BlockItem item) {
            // Torches should try the floor first, then nearby walls. Keep the target fixed
            // so a stale placement request cannot place another torch in a neighbouring cell.
            BlockPlaceContext ctx = item instanceof StandingAndWallBlockItem
                    ? new DirectionalPlaceContext(world, pos, face.getOpposite(), stack, face)
                    : new BlockPlaceContext(world, player, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
            item.place(ctx);
        }
        return world.getBlockState(pos);
    }
}
