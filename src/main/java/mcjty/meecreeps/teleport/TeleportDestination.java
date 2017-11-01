package mcjty.meecreeps.teleport;

import net.minecraft.util.math.BlockPos;

public class TeleportDestination {
    private final String name;
    private final int dimension;
    private final BlockPos pos;

    public TeleportDestination(String name, int dimension, BlockPos pos) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
    }

    public String getName() {
        return name;
    }

    public int getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }
}
