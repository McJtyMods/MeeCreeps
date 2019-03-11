package mcjty.meecreeps.actions;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.api.IActionContext;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.GuiProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
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

public class ActionOptions implements IActionContext {

    private final List<MeeCreepActionType> actionOptions;
    private final List<MeeCreepActionType> maybeActionOptions;
    private final BlockPos targetPos;
    private final EnumFacing targetSide;
    private int dimension;
    private int failureCount;                       // After how many ticks do we give up trying to find this MeeCreep
    @Nullable private final UUID playerId;
    private final int actionId;

    private int timeout;
    private Stage stage;
    private MeeCreepActionType task;
    private String furtherQuestionId;
    private boolean paused;
    private List<Pair<BlockPos, ItemStack>> drops = new ArrayList<>();

    // playerId can be null in case we have a player-less action
    public ActionOptions(List<MeeCreepActionType> actionOptions, List<MeeCreepActionType> maybeActionOptions,
                         BlockPos targetPos, EnumFacing targetSide, int dimension, @Nullable UUID playerId, int actionId) {
        this.actionOptions = actionOptions;
        this.maybeActionOptions = maybeActionOptions;
        this.targetPos = targetPos;
        this.targetSide = targetSide;
        this.dimension = dimension;
        failureCount = 60;
        this.playerId = playerId;
        this.actionId = actionId;
        timeout = 10;
        stage = Stage.WAITING_FOR_SPAWN;
        task = null;
        furtherQuestionId = null;
        paused = false;
    }

    public ActionOptions(ByteBuf buf) {
        int size = buf.readInt();
        actionOptions = new ArrayList<>();
        while (size > 0) {
            actionOptions.add(new MeeCreepActionType(NetworkTools.readStringUTF8(buf)));
            size--;
        }
        size = buf.readInt();
        maybeActionOptions = new ArrayList<>();
        while (size > 0) {
            maybeActionOptions.add(new MeeCreepActionType(NetworkTools.readStringUTF8(buf)));
            size--;
        }
        targetPos = NetworkTools.readPos(buf);
        targetSide = EnumFacing.VALUES[buf.readByte()];
        dimension = buf.readInt();
        failureCount = buf.readInt();
        if (buf.readBoolean()) {
            playerId = new UUID(buf.readLong(), buf.readLong());
        } else {
            playerId = null;
        }
        actionId = buf.readInt();
        timeout = buf.readInt();
        stage = Stage.values()[buf.readByte()];
        if (buf.readBoolean()) {
            task = new MeeCreepActionType(NetworkTools.readStringUTF8(buf));
        }
        furtherQuestionId = NetworkTools.readStringUTF8(buf);
        paused = buf.readBoolean();

        // Drops not needed on client so no persistance
    }

    public ActionOptions(NBTTagCompound tagCompound) {
        NBTTagList list = tagCompound.getTagList("options", Constants.NBT.TAG_STRING);
        actionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            actionOptions.add(new MeeCreepActionType(list.getStringTagAt(i)));
        }

        list = tagCompound.getTagList("maybe", Constants.NBT.TAG_STRING);
        maybeActionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
            maybeActionOptions.add(new MeeCreepActionType(list.getStringTagAt(i)));
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
        failureCount = tagCompound.getInteger("failure");
        targetPos = BlockPos.fromLong(tagCompound.getLong("pos"));
        targetSide = EnumFacing.VALUES[tagCompound.getByte("targetSide")];
        if (tagCompound.hasUniqueId("player")) {
            playerId = tagCompound.getUniqueId("player");
        } else {
            playerId = null;
        }
        actionId = tagCompound.getInteger("actionId");
        timeout = tagCompound.getInteger("timeout");
        stage = Stage.getByCode(tagCompound.getString("stage"));
        if (tagCompound.hasKey("task")) {
            task = new MeeCreepActionType(tagCompound.getString("task"));
        }
        if (tagCompound.hasKey("fqid")) {
            furtherQuestionId = tagCompound.getString("fqid");
        } else {
            furtherQuestionId = null;
        }
        paused = tagCompound.getBoolean("paused");
    }

    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(actionOptions.size());
        for (MeeCreepActionType option : actionOptions) {
            NetworkTools.writeStringUTF8(buf, option.getId());
        }
        buf.writeInt(maybeActionOptions.size());
        for (MeeCreepActionType option : maybeActionOptions) {
            NetworkTools.writeStringUTF8(buf, option.getId());
        }
        NetworkTools.writePos(buf, targetPos);
        buf.writeByte(targetSide.ordinal());
        buf.writeInt(dimension);
        buf.writeInt(failureCount);
        if (playerId != null) {
            buf.writeBoolean(true);
            buf.writeLong(playerId.getMostSignificantBits());
            buf.writeLong(playerId.getLeastSignificantBits());
        } else {
            buf.writeBoolean(false);
        }
        buf.writeInt(actionId);
        buf.writeInt(timeout);
        buf.writeByte(stage.ordinal());
        if (task != null) {
            buf.writeBoolean(true);
            NetworkTools.writeStringUTF8(buf, task.getId());
        } else {
            buf.writeBoolean(false);
        }
        NetworkTools.writeStringUTF8(buf, furtherQuestionId);
        buf.writeBoolean(paused);

        // Drops not needed on client so no persistance
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList list = new NBTTagList();
        for (MeeCreepActionType option : actionOptions) {
            list.appendTag(new NBTTagString(option.getId()));
        }
        tagCompound.setTag("options", list);

        list = new NBTTagList();
        for (MeeCreepActionType option : maybeActionOptions) {
            list.appendTag(new NBTTagString(option.getId()));
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
        tagCompound.setInteger("failure", failureCount);
        tagCompound.setLong("pos", targetPos.toLong());
        tagCompound.setByte("targetSide", (byte) targetSide.ordinal());
        if (playerId != null) {
            tagCompound.setUniqueId("player", playerId);
        }
        tagCompound.setInteger("actionId", actionId);
        tagCompound.setInteger("timeout", timeout);
        tagCompound.setString("stage", stage.getCode());
        if (task != null) {
            tagCompound.setString("task", task.getId());
        }
        if (furtherQuestionId != null) {
            tagCompound.setString("fqid", furtherQuestionId);
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

    public List<Pair<BlockPos, ItemStack>> getDrops() {
        return drops;
    }

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
        return targetPos;
    }

    @Override
    public EnumFacing getTargetSide() {
        return targetSide;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getActionId() {
        return actionId;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.timeout = stage.getTimeout();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public MeeCreepActionType getTask() {
        return task;
    }

    @Nullable
    @Override
    public String getFurtherQuestionId() {
        return furtherQuestionId;
    }

    public void setTask(MeeCreepActionType task, @Nullable String furtherQuestionId) {
        this.task = task;
        this.furtherQuestionId = furtherQuestionId;
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
                    if (spawn(world, getTargetPos(), getTargetSide(), getActionId(), true)) {
                        setStage(Stage.OPENING_GUI);
                    } else {
                        EntityPlayer player = getPlayer();
                        if (player != null) {
                            MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cant_spawn_meecreep"), (EntityPlayerMP) player);
                        }
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
                    EntityPlayerMP player = playerId == null ? null : server.getPlayerList().getPlayerByUUID(playerId);
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
                case TASK_IS_DONE:
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
        if (playerId == null) {
            return null;
        }
        World world = DimensionManager.getWorld(0);
        MinecraftServer server = world.getMinecraftServer();
        return server.getPlayerList().getPlayerByUUID(playerId);
    }

    @Nullable
    public UUID getPlayerId() {
        return playerId;
    }

    private boolean openGui() {
        if (playerId == null) {
            return false;
        }
        MinecraftServer server = DimensionManager.getWorld(0).getMinecraftServer();
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(playerId);
        if (player != null) {
            MeeCreepsMessages.INSTANCE.sendTo(new PacketActionOptionToClient(this, GuiProxy.GUI_MEECREEP_QUESTION), player);
        } else {
            return false;
        }
        return true;
    }

    private static boolean validSpawnPoint(World world, BlockPos p) {
        return world.isAirBlock(p) && (!world.isAirBlock(p.down()) || !world.isAirBlock(p.down(2))) && world.isAirBlock(p.up());
    }

    public static boolean spawn(World world, BlockPos targetPos, EnumFacing targetSide, int actionId, boolean doSound) {
        BlockPos p;
        if (validSpawnPoint(world, targetPos.offset(targetSide))) {
            p = targetPos.offset(targetSide);
        } else if (validSpawnPoint(world, targetPos.north())) {
            p = targetPos.north();
        } else if (validSpawnPoint(world, targetPos.south())) {
            p = targetPos.south();
        } else if (validSpawnPoint(world, targetPos.east())) {
            p = targetPos.east();
        } else if (validSpawnPoint(world, targetPos.west())) {
            p = targetPos.west();
        } else if (validSpawnPoint(world, targetPos.up())) {
            p = targetPos.up();
        } else {
            return false;
        }
        EntityMeeCreeps entity = new EntityMeeCreeps(world);
        entity.setLocationAndAngles(p.getX()+.5, p.getY(), p.getZ()+.5, 0, 0);
        entity.setActionId(actionId);
        world.spawnEntity(entity);

        if (doSound && ConfigSetup.meeCreepVolume.get() > 0.01f) {
            String snd = "intro1";
            switch (entity.getRandom().nextInt(4)) {
                case 0:
                    snd = "intro1";
                    break;
                case 1:
                    snd = "intro2";
                    break;
                case 2:
                    snd = "intro3";
                    break;
                case 3:
                    snd = "intro4";
                    break;
            }
            SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, snd));
            SoundTools.playSound(world, sound, p.getX(), p.getY(), p.getZ(), ConfigSetup.meeCreepVolume.get(), 1);
        }

        return true;
    }
}
