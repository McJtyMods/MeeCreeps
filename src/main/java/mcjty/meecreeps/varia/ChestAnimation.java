package mcjty.meecreeps.varia;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.HashMap;
import java.util.Map;

public class ChestAnimation {
    private static final int OPEN_TICKS = 20;
    private static final int SET_OPEN_COUNT = 1;
    private static final Map<ChestBlockEntity, Long> openChests = new HashMap<>();

    public static void open(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof ChestBlockEntity chest)) {
            return;
        }
        open(chest);
        BlockState state = chest.getBlockState();
        if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos otherPos = pos.relative(ChestBlock.getConnectedDirection(state));
            if (level.getBlockEntity(otherPos) instanceof ChestBlockEntity other
                    && other.getBlockState().is(state.getBlock())) {
                open(other);
            }
        }
    }

    private static void open(ChestBlockEntity chest) {
        Level level = chest.getLevel();
        openChests.put(chest, level.getGameTime() + OPEN_TICKS);
        level.blockEvent(chest.getBlockPos(), chest.getBlockState().getBlock(), SET_OPEN_COUNT, 1);
    }

    public static void tick(ServerLevel level) {
        var iterator = openChests.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            ChestBlockEntity chest = entry.getKey();
            if (chest.getLevel() != level) {
                continue;
            }
            if (chest.isRemoved() || !level.hasChunkAt(chest.getBlockPos())
                    || level.getBlockEntity(chest.getBlockPos()) != chest) {
                iterator.remove();
                continue;
            }
            boolean expired = level.getGameTime() >= entry.getValue();
            // Refresh while active: player menu events also control this same lid.
            // Use the real player count when closing, without changing it ourselves.
            int count = ChestBlockEntity.getOpenCount(level, chest.getBlockPos());
            level.blockEvent(chest.getBlockPos(), chest.getBlockState().getBlock(),
                    SET_OPEN_COUNT, expired ? count : Math.max(1, count));
            if (expired) {
                iterator.remove();
            }
        }
    }

    public static void stopped() {
        openChests.clear();
    }
}
