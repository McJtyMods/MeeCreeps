package mcjty.meecreeps.teleport;

import mcjty.lib.varia.DimensionId;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class TeleportDestination {
    private String name;
    private DimensionId dimension;
    private BlockPos pos;         // The position of the portal tile entity itself
    private Direction side;      // The side on which to render the portal. UP is for a horizontal portal

    public TeleportDestination(String name, DimensionId dimension, BlockPos pos, Direction side) {
        this.name = name;
        this.dimension = dimension;
        this.pos = pos;
        this.side = side;
    }

    public TeleportDestination(CompoundNBT tc) {
        name = tc.getString("name");
        dimension = DimensionId.fromResourceLocation(new ResourceLocation(tc.getString("dim")));
        pos = new BlockPos(tc.getInt("x"), tc.getInt("y"), tc.getInt("z"));
        side = Direction.values()[tc.getByte("side")];
    }

    public CompoundNBT getCompound() {
        CompoundNBT tc = new CompoundNBT();
        tc.putString("name", getName());
        tc.putString("dim", getDimension().getName());
        tc.putByte("side", (byte) getSide().ordinal());
        tc.putInt("x", getPos().getX());
        tc.putInt("y", getPos().getY());
        tc.putInt("z", getPos().getZ());
        return tc;
    }

    public String getName() {
        return name;
    }

    public DimensionId getDimension() {
        return dimension;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getSide() {
        return side;
    }
}
