package mcjty.meecreeps.teleport;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TeleportDestination {
    private final String name;
    private final int dimension;
    private final BlockPos pos;         // The position of the portal tile entity itself
    private final Direction side;      // The side on which to render the portal. UP is for a horizontal portal

    public TeleportDestination(String name, int dimension, BlockPos pos, Direction side) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
        this.side = side;
    }

    public TeleportDestination(CompoundNBT tc) {
        name = tc.getString("name");
        dimension = tc.getInt("dim");
        pos = new BlockPos(tc.getInt("x"), tc.getInt("y"), tc.getInt("z"));
        side = Direction.values()[tc.getByte("side")];
    }

    public CompoundNBT getCompound() {
        CompoundNBT tc = new CompoundNBT();
        tc.putString("name", getName());
        tc.putInt("dim", getDimension());
        tc.putByte("side", (byte) getSide().ordinal());
        tc.putInt("x", getPos().getX());
        tc.putInt("y", getPos().getY());
        tc.putInt("z", getPos().getZ());
        return tc;
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

    public Direction getSide() {
        return side;
    }
}
