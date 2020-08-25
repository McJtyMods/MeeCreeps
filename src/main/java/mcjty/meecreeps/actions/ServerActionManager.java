package mcjty.meecreeps.actions;

import com.google.common.collect.ImmutableList;
import mcjty.lib.varia.DimensionId;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.TeleportationTools;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.entities.ModEntities;
import mcjty.meecreeps.items.CreepCubeItem;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ServerActionManager extends AbstractWorldData<ServerActionManager> {

    private static final String NAME = "MeeCreepsData";

    private List<ActionOptions> options = new ArrayList<>();
    private Map<Integer, ActionOptions> optionMap = new HashMap<>();
    private int lastId = 0;

    private Map<Integer, EntityMeeCreeps> entityCache = new HashMap<>();

    public ServerActionManager(String name) {
        super(name);
    }

    // todo: find replacement
//    @Override
//    public void clear() {
//        options.clear();
//        optionMap.clear();
//        lastId = 0;
//        entityCache.clear();
//    }

    /**
     * If player == null then we're doing this as an OP and in that case we clear all options
     * and additionally kill all remaining MeeCreeps
     * @param source
     * @param player
     */
    // todo: note issues may exist
    public void clearOptions(CommandSource source, @Nullable PlayerEntity player) {
        if (player == null) {
            source.sendFeedback(new StringTextComponent("Cleared " + options.size() + " active operations"), false);
            options.clear();
        } else {
            int cnt = 0;
            List<ActionOptions> toKeep = new ArrayList<>();
            for (ActionOptions option : options) {
                if (!player.getGameProfile().getId().equals(option.getPlayerId())) {
                    toKeep.add(option);
                } else {
                    cnt++;
                }
            }
            options = toKeep;
            source.sendFeedback(new StringTextComponent("Cleared " + cnt + " active operations"), false);
        }
        save();

        if (player == null) {
            int entitiesRemoved = 0;
            for (ServerWorld world : source.getServer().getWorlds()) {
                for (Entity entity : world.getEntities(ModEntities.MEECREEPS_ENTITY.get(), i -> true)) {
                    entity.remove();
                    entitiesRemoved ++;
                }
            }

            source.sendFeedback(new StringTextComponent("Additionally killed " + entitiesRemoved + " MeeCreeps"), false);
        }
    }

    public void listOptions(CommandSource sender) {
        for (Map.Entry<Integer, ActionOptions> entry : optionMap.entrySet()) {
            ActionOptions options = entry.getValue();
            Stage stage = options.getStage();
            MeeCreepActionType task = options.getTask();
            EntityMeeCreeps entity = findMeeCreep(sender.getWorld(), entry.getKey(), options.getDimension());
            String name = entity == null ? "<none>" : entity.getUniqueID().toString();
            sender.sendFeedback(new StringTextComponent("Action " + entry.getKey() + ", Task " + task.getId() + ", Stage " + stage + ", Entity " + name), false);
        }
    }

    public void updateEntityCache(int actionId, @Nullable EntityMeeCreeps entity) {
        if (entity == null) {
            entityCache.remove(actionId);
        } else {
            entityCache.put(actionId, entity);
        }
    }

    public EntityMeeCreeps getCachedEntity(int actionId) {
        return entityCache.get(actionId);
    }

    public int newId() {
        lastId++;
        save();
        return lastId;
    }

    public ActionOptions getOptions(int id) {
        return optionMap.get(id);
    }

    public int countMeeCreeps(PlayerEntity player) {
        int cnt = 0;
        for (ActionOptions option : options) {
            if (Objects.equals(option.getPlayerId(), player.getGameProfile().getId())) {
                cnt++;
            }
        }
        return cnt;
    }

    @Nonnull
    public static ServerActionManager getManager(World world) {
        return getData(world, () -> new ServerActionManager(NAME), NAME);
    }

    public int createActionOptions(World world, BlockPos pos, Direction side, @Nullable PlayerEntity player) {
        List<MeeCreepActionType> types = new ArrayList<>();
        List<MeeCreepActionType> maybeTypes = new ArrayList<>();
        for (MeeCreepsApi.Factory type : MeeCreeps.api.getFactories()) {
            if (ConfigSetup.allowedActions.contains(type.getId())) {
                if (type.getFactory().isPossible(world, pos, side)) {
                    types.add(new MeeCreepActionType(type.getId()));
                } else if (type.getFactory().isPossibleSecondary(world, pos, side)) {
                    maybeTypes.add(new MeeCreepActionType(type.getId()));
                }
            }
        }
        int actionId = newId();
        ActionOptions opt = new ActionOptions(types, maybeTypes, pos, side, DimensionId.fromWorld(world), player == null ? null : player.getUniqueID(), actionId);
        options.add(opt);
        optionMap.put(actionId, opt);
        save();
        return actionId;
    }

    private static Random random = new Random();

    public void performAction(@Nullable ServerPlayerEntity player, int id, MeeCreepActionType type, @Nullable String furtherQuestionId) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            option.setStage(Stage.WORKING);
            option.setTask(type, furtherQuestionId);
            save();

            if (player != null) {
                // Remember the last used action in the MeeCreep cube
                ItemStack cube = CreepCubeItem.getCube(player);
                if (!cube.isEmpty()) {
                    CreepCubeItem.setLastAction(cube, type, furtherQuestionId);
                }

                if (ConfigSetup.meeCreepVolume.get() > 0.01f) {
                    String snd = "ok";
                    switch (random.nextInt(2)) {
                        case 0:
                            snd = "ok";
                            break;
                        case 1:
                            snd = "ok2";
                            break;
                    }
                    SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(MeeCreeps.MODID, snd));
                    SoundTools.playSound(player.getEntityWorld(), sound, player.getPosX(), player.getPosY(), player.getPosZ(), ConfigSetup.meeCreepVolume.get(), 1);
                }
            }
        }
    }

    public void cancelAction(ServerPlayerEntity player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            option.setStage(Stage.DONE);
            option.setPaused(false);
        }
    }

    public void resumeAction(ServerPlayerEntity player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            option.setPaused(false);
        }
    }

    // The dimension parameter is the dimension where the meecreep was last seen
    private EntityMeeCreeps findMeeCreep(ServerWorld world, int actionId, DimensionId dimension) {
        EntityMeeCreeps cachedEntity = getCachedEntity(actionId);
        if (cachedEntity != null && cachedEntity.isAlive()) {
            return cachedEntity;
        }

        // Worlds to search
        LinkedHashSet<ServerWorld> worlds = new LinkedHashSet<>();

        worlds.add(world);
        worlds.addAll(ImmutableList.copyOf(world.getServer().getWorlds()));
        worlds.add(dimension.getWorld());

        for (ServerWorld serverWorld : worlds) {
            EntityMeeCreeps entity = this.fetchFirstCreep(serverWorld, actionId);

            if (entity != null) {
                updateEntityCache(actionId, entity);
                return entity;
            }
        }

        return null;
    }

    @Nullable
    private EntityMeeCreeps fetchFirstCreep(ServerWorld world, int actionId) {
        // So we have to assume that the entity is a meeCreep as the getEntities does do an active filter
        // Casting from here on out should be assumed to be safe...
        List<Entity> entities = world.getEntities(ModEntities.MEECREEPS_ENTITY.get(), input -> input != null && ((EntityMeeCreeps) input).getActionId() == actionId && input.isAlive());
        if (!entities.isEmpty()) {
            return (EntityMeeCreeps) entities.get(0);
        }

        return null;
    }

    public void tick() {
        save();
        List<ActionOptions> newlist = new ArrayList<>();
        Map<Integer, ActionOptions> newmap = new HashMap<>();
        for (ActionOptions option : options) {
            EntityMeeCreeps meeCreep = findMeeCreep(DimensionId.overworld().getWorld(), option.getActionId(), option.getDimension());
            boolean keep = true;

            World world = meeCreep == null ? DimensionId.overworld().getWorld() : meeCreep.getEntityWorld();
            BlockPos meeCreepPos = meeCreep == null ? option.getTargetPos() : meeCreep.getPosition();
            if (world != null && world.isBlockLoaded(meeCreepPos)) {
                if (!option.tick(world)) {
                    keep = false;
                }
            } else {
                if (option.getStage() != Stage.OPENING_GUI && option.getStage() != Stage.WAITING_FOR_PLAYER_INPUT && option.getStage() != Stage.WAITING_FOR_SPAWN) {
                    keep = false;
                }
            }
            if (meeCreep != null) {
                stayWithPlayer(option, meeCreep);
            } else if (option.getStage() != Stage.OPENING_GUI && option.getStage() != Stage.WAITING_FOR_PLAYER_INPUT && option.getStage() != Stage.WAITING_FOR_SPAWN) {
                keep = false;
            }

            if (meeCreep == null) {
                int failureCount = option.getFailureCount();
                failureCount--;
                option.setFailureCount(failureCount);
                if (failureCount <= 0) {
                    System.out.println("ServerActionManager.tick: FAILURE");
                    keep = false;
                }
            }

            if (keep) {
                newlist.add(option);
                newmap.put(option.getActionId(), option);
            } else if (world != null) {
                dropRemainingDrops(option, world);
            }
        }
        options = newlist;
        optionMap = newmap;
    }

    private void dropRemainingDrops(ActionOptions option, World world) {
        List<Pair<BlockPos, ItemStack>> drops = option.getDrops();
        if (!drops.isEmpty()) {
            for (Pair<BlockPos, ItemStack> pair : drops) {
                ItemEntity entityItem = new ItemEntity(world, pair.getLeft().getX(), pair.getLeft().getY(), pair.getLeft().getZ());
                entityItem.setItem(pair.getValue());
                BlockPos pos = pair.getKey();
                entityItem.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                world.addEntity(entityItem);
            }
        }
    }

    private void stayWithPlayer(ActionOptions option, EntityMeeCreeps meeCreep) {
        // We check here if the MeeCreep wants to follow the player
        // and if so we do the teleport here
        PlayerEntity player = option.getPlayer();
        if (player != null) {
            if (meeCreep.getHelper() != null) {
                IActionWorker worker = meeCreep.getHelper().getWorker();
                if (worker.needsToFollowPlayer()) {
                    if (isDifferentDimension(player, meeCreep) || isTooFar(player, meeCreep)) {
                        // Wrong dimension. Teleport to the player
                        meeCreep.cancelJob();
                        BlockPos p = WorkerHelper.findSuitablePositionNearPlayer(meeCreep, player, 4.0);
                        meeCreep = (EntityMeeCreeps) TeleportationTools.teleportEntity(meeCreep, player.getEntityWorld(), p.getX() + .5, p.getY(), p.getZ() + .5, Direction.NORTH);
                        updateEntityCache(option.getActionId(), meeCreep);
                        option.setDimension(DimensionId.fromWorld(player.getEntityWorld()));
                    }
                }
            }
        }
    }

    private boolean isDifferentDimension(PlayerEntity player, EntityMeeCreeps meeCreep) {
        return !DimensionId.sameDimension(player.getEntityWorld(), meeCreep.getEntityWorld());
    }

    private boolean isTooFar(PlayerEntity player, EntityMeeCreeps meeCreep) {
        return player.getPositionVector().squareDistanceTo(meeCreep.getPositionVector()) > 60*60;
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT list = nbt.getList("actions", Constants.NBT.TAG_COMPOUND);
        options = new ArrayList<>();
        optionMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            ActionOptions opt = new ActionOptions(list.getCompound(i));
            options.add(opt);
            optionMap.put(opt.getActionId(), opt);
        }
        lastId = nbt.getInt("lastId");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();
        for (ActionOptions option : options) {
            CompoundNBT tc = new CompoundNBT();
            option.writeToNBT(tc);
            list.add(tc);
        }
        compound.put("actions", list);
        compound.putInt("lastId", lastId);
        return compound;
    }
}
