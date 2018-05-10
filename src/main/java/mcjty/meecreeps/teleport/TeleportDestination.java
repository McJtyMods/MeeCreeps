package mcjty.meecreeps.teleport;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TeleportDestination {
    private final String name;
    private final int dimension;
    private final BlockPos pos;         // The position of the portal tile entity itself
    private final EnumFacing side;      // The side on which to render the portal. UP is for a horizontal portal

    public TeleportDestination(String name, int dimension, BlockPos pos, EnumFacing side) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
        this.side = side;
    }

    public TeleportDestination(NBTTagCompound tc) {
        name = tc.getString("name");
        dimension = tc.getInteger("dim");
        pos = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
        side = EnumFacing.VALUES[tc.getByte("side")];
    }

    public NBTTagCompound getCompound() {
        NBTTagCompound tc = new NBTTagCompound();
        tc.setString("name", getName());
        tc.setInteger("dim", getDimension());
        tc.setByte("side", (byte) getSide().ordinal());
        tc.setInteger("x", getPos().getX());
        tc.setInteger("y", getPos().getY());
        tc.setInteger("z", getPos().getZ());
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

    public EnumFacing getSide() {
        return side;
    }
}
