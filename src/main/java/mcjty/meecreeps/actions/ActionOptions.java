package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.api.IActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.NetworkTools;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.proxy.GuiProxy;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActionOptions implements IActionOptions {

    private final List<MeeCreepActionType> actionOptions;
    private final List<MeeCreepActionType> maybeActionOptions;
    private final BlockPos pos;
    private final int dimension;
    private final UUID playerId;
    private final int actionId;

    private int timeout;
    private Stage stage;
    private MeeCreepActionType task;
    private boolean paused;
    private List<Pair<BlockPos, ItemStack>> drops = new ArrayList<>();

    public ActionOptions(List<MeeCreepActionType> actionOptions, List<MeeCreepActionType> maybeActionOptions, BlockPos pos, int dimension, UUID playerId, int actionId) {
        this.actionOptions = actionOptions;
        this.maybeActionOptions = maybeActionOptions;
        this.pos = pos;
        this.dimension = dimension;
        this.playerId = playerId;
        this.actionId = actionId;
        timeout = 10;
        stage = Stage.WAITING_FOR_SPAWN;
        task = null;
        paused = false;
    }

    public ActionOptions(ByteBuf buf) {
        int size = buf.readInt();
        actionOptions = new ArrayList<>();
        while (size > 0) {
            actionOptions.add(MeeCreepActionType.VALUES[buf.readByte()]);
            size--;
        }
        size = buf.readInt();
        maybeActionOptions = new ArrayList<>();
        while (size > 0) {
            maybeActionOptions.add(MeeCreepActionType.VALUES[buf.readByte()]);
            size--;
        }
        pos = NetworkTools.readPos(buf);
        dimension = buf.readInt();
        playerId = new UUID(buf.readLong(), buf.readLong());
        actionId = buf.readInt();
        timeout = buf.readInt();
        stage = Stage.values()[buf.readByte()];
        if (buf.readBoolean()) {
            task = MeeCreepActionType.VALUES[buf.readByte()];
        }
        paused = buf.readBoolean();

        // Drops not needed on client so no persistance
    }

    public ActionOptions(NBTTagCompound tagCompound) {
        NBTTagList list = tagCompound.getTagList("options", Constants.NBT.TAG_STRING);
        actionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            actionOptions.add(MeeCreepActionType.getByCode(list.getStringTagAt(i)));
        }

        list = tagCompound.getTagList("maybe", Constants.NBT.TAG_STRING);
        maybeActionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            maybeActionOptions.add(MeeCreepActionType.getByCode(list.getStringTagAt(i)));
        }

        list = tagCompound.getTagList("drops", Constants.NBT.TAG_COMPOUND);
        drops = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            BlockPos p = BlockPos.fromLong(tc.getLong("p"));
            NBTTagCompound itemTag = tc.getCompoundTag("i");
            ItemStack stack = new ItemStack(itemTag);
            drops.add(Pair.of(p, stack));
        }

        dimension = tagCompound.getInteger("dim");
        pos = BlockPos.fromLong(tagCompound.getLong("pos"));
        playerId = tagCompound.getUniqueId("player");
        actionId = tagCompound.getInteger("actionId");
        timeout = tagCompound.getInteger("timeout");
        stage = Stage.getByCode(tagCompound.getString("stage"));
        if (tagCompound.hasKey("task")) {
            task = MeeCreepActionType.getByCode(tagCompound.getString("task"));
        }
        paused = tagCompound.getBoolean("paused");
    }

    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(actionOptions.size());
        for (MeeCreepActionType option : actionOptions) {
            buf.writeByte(option.ordinal());
        }
        buf.writeInt(maybeActionOptions.size());
        for (MeeCreepActionType option : maybeActionOptions) {
            buf.writeByte(option.ordinal());
        }
        NetworkTools.writePos(buf, pos);
        buf.writeInt(dimension);
        buf.writeLong(playerId.getMostSignificantBits());
        buf.writeLong(playerId.getLeastSignificantBits());
        buf.writeInt(actionId);
        buf.writeInt(timeout);
        buf.writeByte(stage.ordinal());
        if (task != null) {
            buf.writeBoolean(true);
            buf.writeByte(task.ordinal());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeBoolean(paused);

        // Drops not needed on client so no persistance
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList list = new NBTTagList();
        for (MeeCreepActionType option : actionOptions) {
            list.appendTag(new NBTTagString(option.getCode()));
        }
        tagCompound.setTag("options", list);

        list = new NBTTagList();
        for (MeeCreepActionType option : maybeActionOptions) {
            list.appendTag(new NBTTagString(option.getCode()));
        }
        tagCompound.setTag("maybe", list);

        list = new NBTTagList();
        for (Pair<BlockPos, ItemStack> pair : drops) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setLong("p", pair.getKey().toLong());
            tc.setTag("i", pair.getValue().writeToNBT(new NBTTagCompound()));
            list.appendTag(tc);
        }
        tagCompound.setTag("drops", list);

        tagCompound.setInteger("dim", dimension);
        tagCompound.setLong("pos", pos.toLong());
        tagCompound.setUniqueId("player", playerId);
        tagCompound.setInteger("actionId", actionId);
        tagCompound.setInteger("timeout", timeout);
        tagCompound.setString("stage", stage.getCode());
        if (task != null) {
            tagCompound.setString("task", task.getCode());
        }
        tagCompound.setBoolean("paused", paused);
    }

    public void registerDrops(BlockPos pos, @Nonnull List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            // Drops can be empty because they can be 'consumed' by the entity
            if (!drop.isEmpty()) {
                this.drops.add(Pair.of(pos, drop.copy()));
            }
        }
    }

    @Override
    public List<Pair<BlockPos, ItemStack>> getDrops() {
        return drops;
    }

    @Override
    public void clearDrops() {
        drops.clear();
    }

    public List<MeeCreepActionType> getActionOptions() {
        return actionOptions;
    }

    public List<MeeCreepActionType> getMaybeActionOptions() {
        return maybeActionOptions;
    }

    @Override
    public BlockPos getTargetPos() {
        return pos;
    }

    public int getDimension() {
        return dimension;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @Override
    public int getActionId() {
        return actionId;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        this.timeout = stage.getTimeout();
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public MeeCreepActionType getTask() {
        return task;
    }

    public void setTask(MeeCreepActionType task) {
        this.task = task;
    }

    public boolean tick(World world) {
        if (paused) {
            return true;
        }
        timeout--;
        if (timeout <= 0) {
            timeout = 20;
            switch (stage) {
                case WAITING_FOR_SPAWN:
                    if (spawn(world)) {
                        setStage(Stage.OPENING_GUI);
                    } else {
                        return false;
                    }
                    break;
                case OPENING_GUI:
                    if (!openGui()) {
                        return false;
                    }
                    setStage(Stage.WAITING_FOR_PLAYER_INPUT);
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
                    // It is up to the entity to set stage to DONE when done early
                    setStage(Stage.TIME_IS_UP);
                    break;
                case TIME_IS_UP:
                    break;
                case DONE:
                    return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public EntityPlayer getPlayer() {
        World world = DimensionManager.getWorld(0);
        MinecraftServer server = world.getMinecraftServer();
        return server.getPlayerList().getPlayerByUUID(getPlayerId());
    }

    private boolean openGui() {
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(getPlayerId());
        if (player != null) {
            PacketHandler.INSTANCE.sendTo(new PacketActionOptionToClient(this, GuiProxy.GUI_MEECREEP_QUESTION), player);
        } else {
            return false;
        }
        return true;
    }

    private boolean validSpawnPoint(World world, BlockPos p) {
        return world.isAirBlock(p) && (!world.isAirBlock(p.down())) && world.isAirBlock(p.up());
    }

    private boolean spawn(World world) {
        BlockPos p;
        if (validSpawnPoint(world, getTargetPos().north())) {
            p = getTargetPos().north();
        } else if (validSpawnPoint(world, getTargetPos().south())) {
            p = getTargetPos().south();
        } else if (validSpawnPoint(world, getTargetPos().east())) {
            p = getTargetPos().east();
        } else if (validSpawnPoint(world, getTargetPos().west())) {
            p = getTargetPos().west();
        } else if (validSpawnPoint(world, getTargetPos().up())) {
            p = getTargetPos().up();
        } else {
            return false;
        }
        EntityMeeCreeps entity = new EntityMeeCreeps(world);
        entity.setLocationAndAngles(p.getX()+.5, p.getY(), p.getZ()+.5, 0, 0);
        entity.setActionId(getActionId());
        world.spawnEntity(entity);

        SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, "intro1"));
        // @todo config
        SoundTools.playSound(world, sound, p.getX(), p.getY(), p.getZ(), 1, 1);

        return true;
    }
}
