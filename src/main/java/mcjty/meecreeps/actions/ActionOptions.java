package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.NetworkTools;
import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActionOptions {

    private final List<MeeCreepActionType> actionOptions;
    private final BlockPos pos;
    private final int dimension;
    private final UUID playerId;
    private final int actionId;

    private int timeout;
    private Stage stage;

    public ActionOptions(List<MeeCreepActionType> actionOptions, BlockPos pos, int dimension, UUID playerId, int actionId) {
        this.actionOptions = actionOptions;
        this.pos = pos;
        this.dimension = dimension;
        this.playerId = playerId;
        this.actionId = actionId;
        timeout = 10;
        stage = Stage.WAITING_FOR_SPAWN;
    }

    public ActionOptions(ByteBuf buf) {
        int size = buf.readInt();
        actionOptions = new ArrayList<>();
        while (size > 0) {
            actionOptions.add(MeeCreepActionType.VALUES[buf.readByte()]);
            size--;
        }
        pos = NetworkTools.readPos(buf);
        dimension = buf.readInt();
        playerId = new UUID(buf.readLong(), buf.readLong());
        actionId = buf.readInt();
        timeout = buf.readInt();
        stage = Stage.values()[buf.readByte()];
    }

    public ActionOptions(NBTTagCompound tagCompound) {
        NBTTagList list = tagCompound.getTagList("options", Constants.NBT.TAG_STRING);
        actionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            actionOptions.add(MeeCreepActionType.getByCode(list.getStringTagAt(i)));
        }
        dimension = tagCompound.getInteger("dim");
        pos = BlockPos.fromLong(tagCompound.getLong("pos"));
        playerId = tagCompound.getUniqueId("player");
        actionId = tagCompound.getInteger("actionId");
        timeout = tagCompound.getInteger("timeout");
        stage = Stage.getByCode(tagCompound.getString("stage"));
    }

    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(actionOptions.size());
        for (MeeCreepActionType option : actionOptions) {
            buf.writeByte(option.ordinal());
        }
        NetworkTools.writePos(buf, pos);
        buf.writeInt(dimension);
        buf.writeLong(playerId.getMostSignificantBits());
        buf.writeLong(playerId.getLeastSignificantBits());
        buf.writeInt(actionId);
        buf.writeInt(timeout);
        buf.writeByte(stage.ordinal());
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList list = new NBTTagList();
        for (MeeCreepActionType option : actionOptions) {
            list.appendTag(new NBTTagString(option.getCode()));
        }
        tagCompound.setTag("options", list);
        tagCompound.setInteger("dim", dimension);
        tagCompound.setLong("pos", pos.toLong());
        tagCompound.setUniqueId("player", playerId);
        tagCompound.setInteger("actionId", actionId);
        tagCompound.setInteger("timeout", timeout);
        tagCompound.setString("stage", stage.getCode());
    }

    public List<MeeCreepActionType> getActionOptions() {
        return actionOptions;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getDimension() {
        return dimension;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getActionId() {
        return actionId;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean tick(World world) {
        timeout--;
        if (timeout <= 0) {
            switch (stage) {
                case WAITING_FOR_SPAWN:
                    spawn(world);
                    stage = Stage.OPENING_GUI;
                    break;
                case OPENING_GUI:
                    if (!openGui()) {
                        return false;
                    }
                    stage = Stage.WAITING_FOR_PLAYER_INPUT;
                    break;
                case WAITING_FOR_PLAYER_INPUT:
                    // @todo some kind of timeout as well?
                    MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
                    EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(getPlayerId());
                    if (player == null) {
                        // If player is gone we stop
                        return false;
                    }
                    break;
                case WORKING:
                    stage = Stage.DONE;
                    break;
                case DONE:
                    return false;
            }
            timeout = 20;
        }
        return true;
    }

    private boolean openGui() {
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(getPlayerId());
        if (player != null) {
            PacketHandler.INSTANCE.sendTo(new PacketActionOptionToClient(this), player);
        } else {
            return false;
        }
        return true;
    }

    private void spawn(World world) {
        EntityMeeCreeps entity = new EntityMeeCreeps(world);
        BlockPos p = getPos().up();
        entity.setLocationAndAngles(p.getX(), p.getY(), p.getZ(), 0, 0);
        entity.setActionId(getActionId());
        world.spawnEntity(entity);
    }
}
