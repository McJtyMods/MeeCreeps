package mcjty.meecreeps.teleport;

import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class TeleportDestination {
    private final String name;
    private final net.minecraft.resources.ResourceKey<Level> dimension;
    private final BlockPos pos;         // The position of the portal tile entity itself
    private final Direction side;      // The side on which to render the portal. UP is for a horizontal portal

    public TeleportDestination(String name, net.minecraft.resources.ResourceKey<Level> dimension, BlockPos pos, Direction side) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
        this.side = side;
    }

    public TeleportDestination(CompoundTag tc) {
        name = tc.getStringOr("name", "");
        dimension = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.Identifier.parse(tc.getStringOr("dim", "")));
        pos = new BlockPos(tc.getIntOr("x", 0), tc.getIntOr("y", 0), tc.getIntOr("z", 0));
        side = Direction.values()[tc.getByteOr("side", (byte) 0)];
    }

    public CompoundTag getCompound() {
        CompoundTag tc = new CompoundTag();
        tc.putString("name", getName());
        tc.putString("dim", getDimension().identifier().toString());
        tc.putByte("side", (byte) getSide().ordinal());
        tc.putInt("x", getPos().getX());
        tc.putInt("y", getPos().getY());
        tc.putInt("z", getPos().getZ());
        return tc;
    }

    public String getName() {
        return name;
    }

    public net.minecraft.resources.ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }
}
