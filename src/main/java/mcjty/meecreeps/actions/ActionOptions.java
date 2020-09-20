package mcjty.meecreeps.actions;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.WorldTools;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.api.IActionContext;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.PacketActionOptionToClient;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.network.PacketShowBalloonToClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
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
    private final Direction targetSide;
    private DimensionId dimension;
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
                         BlockPos targetPos, Direction targetSide, DimensionId dimension, @Nullable UUID playerId, int actionId) {
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

    public ActionOptions(PacketBuffer buf) {
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
        targetPos = buf.readBlockPos();
        targetSide = Direction.values()[buf.readByte()];
        dimension = DimensionId.fromPacket(buf);
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

    public ActionOptions(CompoundNBT tagCompound) {
        ListNBT list = tagCompound.getList("options", Constants.NBT.TAG_STRING);
        actionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.size() ; i++) {
            actionOptions.add(new MeeCreepActionType(list.getString(i)));
        }

        list = tagCompound.getList("maybe", Constants.NBT.TAG_STRING);
        maybeActionOptions = new ArrayList<>();
        for (int i = 0 ; i < list.size() ; i++) {
            maybeActionOptions.add(new MeeCreepActionType(list.getString(i)));
        }

        list = tagCompound.getList("drops", Constants.NBT.TAG_COMPOUND);
        drops = new ArrayList<>();
        for (int i = 0 ; i < list.size() ; i++) {
            CompoundNBT tc = list.getCompound(i);
            BlockPos p = BlockPos.fromLong(tc.getLong("p"));
            CompoundNBT itemTag = tc.getCompound("i");
            ItemStack stack = ItemStack.read(itemTag);
            drops.add(Pair.of(p, stack));
        }

        dimension = DimensionId.fromResourceLocation(new ResourceLocation(tagCompound.getString("dim")));
        failureCount = tagCompound.getInt("failure");
        targetPos = BlockPos.fromLong(tagCompound.getLong("pos"));
        targetSide = Direction.values()[tagCompound.getByte("targetSide")];
        if (tagCompound.hasUniqueId("player")) {
            playerId = tagCompound.getUniqueId("player");
        } else {
            playerId = null;
        }
        actionId = tagCompound.getInt("actionId");
        timeout = tagCompound.getInt("timeout");
        stage = Stage.getByCode(tagCompound.getString("stage"));
        if (tagCompound.contains("task")) {
            task = new MeeCreepActionType(tagCompound.getString("task"));
        }
        if (tagCompound.contains("fqid")) {
            furtherQuestionId = tagCompound.getString("fqid");
        } else {
            furtherQuestionId = null;
        }
        paused = tagCompound.getBoolean("paused");
    }

    public void writeToBuf(PacketBuffer buf) {
        buf.writeInt(actionOptions.size());
        for (MeeCreepActionType option : actionOptions) {
            NetworkTools.writeStringUTF8(buf, option.getId());
        }
        buf.writeInt(maybeActionOptions.size());
        for (MeeCreepActionType option : maybeActionOptions) {
            NetworkTools.writeStringUTF8(buf, option.getId());
        }
        buf.writeBlockPos(targetPos);
        buf.writeByte(targetSide.ordinal());
        buf.writeInt(dimension.getInternalId());
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

    public void writeToNBT(CompoundNBT tagCompound) {
        ListNBT list = new ListNBT();
        for (MeeCreepActionType option : actionOptions) {
            list.add(StringNBT.valueOf(option.getId()));
        }
        tagCompound.put("options", list);

        list = new ListNBT();
        for (MeeCreepActionType option : maybeActionOptions) {
            list.add(StringNBT.valueOf(option.getId()));
        }
        tagCompound.put("maybe", list);

        list = new ListNBT();
        for (Pair<BlockPos, ItemStack> pair : drops) {
            CompoundNBT tc = new CompoundNBT();
            tc.putLong("p", pair.getKey().toLong());
            tc.put("i", pair.getValue().write(new CompoundNBT()));
            list.add(tc);
        }
        tagCompound.put("drops", list);

        tagCompound.putString("dim", dimension.getName());
        tagCompound.putInt("failure", failureCount);
        tagCompound.putLong("pos", targetPos.toLong());
        tagCompound.putByte("targetSide", (byte) targetSide.ordinal());
        if (playerId != null) {
            tagCompound.putUniqueId("player", playerId);
        }
        tagCompound.putInt("actionId", actionId);
        tagCompound.putInt("timeout", timeout);
        tagCompound.putString("stage", stage.getCode());
        if (task != null) {
            tagCompound.putString("task", task.getId());
        }
        if (furtherQuestionId != null) {
            tagCompound.putString("fqid", furtherQuestionId);
        }
        tagCompound.putBoolean("paused", paused);
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
    public Direction getTargetSide() {
        return targetSide;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public DimensionId getDimension() {
        return dimension;
    }

    public void setDimension(DimensionId dimension) {
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
                        PlayerEntity player = getPlayer();
                        if (player != null) {
                            PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient("message.meecreeps.cant_spawn_meecreep"), ((ServerPlayerEntity) player).connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
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
                    MinecraftServer server = WorldTools.getOverworld().getServer();
                    ServerPlayerEntity player = playerId == null ? null : server.getPlayerList().getPlayerByUUID(playerId);
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
    public PlayerEntity getPlayer() {
        if (playerId == null) {
            return null;
        }
        World world = WorldTools.getOverworld();
        MinecraftServer server = world.getServer();
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
        MinecraftServer server = WorldTools.getOverworld().getServer();
        ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(playerId);
        if (player != null) {
            // 1 = GUI_MEECREEP_QUESTION
            PacketHandler.INSTANCE.sendTo(new PacketActionOptionToClient(this, 1), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        } else {
            return false;
        }
        return true;
    }

    private static boolean validSpawnPoint(World world, BlockPos p) {
        return world.isAirBlock(p) && (!world.isAirBlock(p.down()) || !world.isAirBlock(p.down(2))) && world.isAirBlock(p.up());
    }

    public static boolean spawn(World world, BlockPos targetPos, Direction targetSide, int actionId, boolean doSound) {
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
        world.addEntity(entity);

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
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MeeCreeps.MODID, snd));
            SoundTools.playSound(world, sound, p.getX(), p.getY(), p.getZ(), ConfigSetup.meeCreepVolume.get(), 1);
        }

        return true;
    }
}
