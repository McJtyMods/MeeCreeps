package mcjty.meecreeps.actions;

import mcjty.meecreeps.setup.Registration;
import mcjty.lib.varia.SoundTools;
import mcjty.meecreeps.varia.EntityTeleportation;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.items.CreepCubeItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import mcjty.meecreeps.varia.LevelTools;
import net.minecraft.nbt.Tag;
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

    public ServerActionManager() {
        super();
    }

    public void clear() {
        options.clear();
        optionMap.clear();
        lastId = 0;
        entityCache.clear();
    }

    /**
     * If player == null then we're doing this as an OP and in that case we clear all options
     * and additionally kill all remaining MeeCreeps
     * @param sender
     * @param player
     */
    public void clearOptions(CommandSourceStack sender, @Nullable Player player) {
        if (player == null) {
            sender.sendSystemMessage(Component.literal("Cleared " + options.size() + " active operations"));
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
            sender.sendSystemMessage(Component.literal("Cleared " + cnt + " active operations"));
        }
        optionMap.clear();
        for (ActionOptions option : options)
            optionMap.put(option.getActionId(), option);
        save();

        if (player == null) {
            int cnt = 0;
            for (ServerLevel w : LevelTools.server().getAllLevels()) {
                for (EntityMeeCreeps entity : w.getEntities(Registration.CREEP.get(), e -> true)) {
                    entity.killMe();
                    cnt++;
                }
            }
            sender.sendSystemMessage(Component.literal("Additionally killed " + cnt + " MeeCreeps"));
        }
    }

    public void listOptions(CommandSourceStack sender) {
        for (Map.Entry<Integer, ActionOptions> entry : optionMap.entrySet()) {
            ActionOptions options = entry.getValue();
            Stage stage = options.getStage();
            MeeCreepActionType task = options.getTask();
            EntityMeeCreeps entity = findMeeCreep(sender.getLevel(), entry.getKey(), options.getDimension());
            String name = entity == null ? "<none>" : entity.getUUID().toString();
            sender.sendSystemMessage(Component.literal("Action " + entry.getKey() + ", Task " + (task == null ? "pending" : task.getId()) + ", Stage " + stage + ", Entity " + name));
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

    public int countMeeCreeps(Player player) {
        int cnt = 0;
        for (ActionOptions option : options) {
            if (Objects.equals(option.getPlayerId(), player.getGameProfile().getId())) {
                cnt++;
            }
        }
        return cnt;
    }

    @Nonnull
    public static ServerActionManager getManager() {
        return getData(LevelTools.overworld(), tag -> {
            var data = new ServerActionManager();
            data.readFromNBT(tag, LevelTools.overworld().registryAccess());
            return data;
        }, ServerActionManager::new, NAME);
    }

    public int createActionOptions(Level world, BlockPos pos, Direction side, @Nullable Player player) {
        List<MeeCreepActionType> types = new ArrayList<>();
        List<MeeCreepActionType> maybeTypes = new ArrayList<>();
        for (MeeCreepsApi.Factory type : MeeCreeps.api.getFactories()) {
            if (ConfigSetup.isAllowed(type.getId())) {
                if (type.getFactory().isPossible(world, pos, side)) {
                    types.add(new MeeCreepActionType(type.getId()));
                } else if (type.getFactory().isPossibleSecondary(world, pos, side)) {
                    maybeTypes.add(new MeeCreepActionType(type.getId()));
                }
            }
        }
        int actionId = newId();
        ActionOptions opt = new ActionOptions(types, maybeTypes, pos, side, world.dimension(), player == null ? null : player.getUUID(), actionId);
        options.add(opt);
        optionMap.put(actionId, opt);
        save();
        return actionId;
    }

    private static Random random = new Random();

    public void performAction(@Nullable ServerPlayer player, int id, MeeCreepActionType type, @Nullable String furtherQuestionId) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            if (player != null && !Objects.equals(option.getPlayerId(), player.getUUID()))
                return;
            var factory = MeeCreeps.api.getFactory(type);
            if (factory == null || !ConfigSetup.isAllowed(type.getId()))
                return;
            Level actionWorld = LevelTools.getWorld(option.getDimension());
            if (actionWorld == null)
                return;
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
                    SoundEvent sound = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(MeeCreeps.MODID, snd));
                    SoundTools.playSound(player.level(), sound, player.getX(), player.getY(), player.getZ(), ConfigSetup.meeCreepVolume.get(), 1);
                }
            }
        }
    }

    public void cancelAction(ServerPlayer player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null && Objects.equals(option.getPlayerId(), player.getUUID())) {
            option.setStage(Stage.DONE);
            option.setPaused(false);
            save();
        }
    }

    public void resumeAction(ServerPlayer player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null && Objects.equals(option.getPlayerId(), player.getUUID())) {
            option.setPaused(false);
            save();
        }
    }

    // The dimension parameter is the dimension where the meecreep was last seen
    private EntityMeeCreeps findMeeCreep(Level world, int actionId, net.minecraft.resources.ResourceKey<Level> dimension) {
        EntityMeeCreeps cachedEntity = getCachedEntity(actionId);
        if (cachedEntity != null && !cachedEntity.isRemoved()) {
            return cachedEntity;
        }
        List<? extends EntityMeeCreeps> entities = ((ServerLevel) world).getEntities(Registration.CREEP.get(), input -> input != null && input.getActionId() == actionId && !input.isRemoved());
        if (!entities.isEmpty()) {
            updateEntityCache(actionId, entities.get(0));
            return entities.get(0);
        }
        // Lets try to find the entity in other dimensions that are still loaded
        for (ServerLevel w : LevelTools.server().getAllLevels()) {
            entities = ((ServerLevel) w).getEntities(Registration.CREEP.get(), input -> input != null && input.getActionId() == actionId && !input.isRemoved());
            if (!entities.isEmpty()) {
                updateEntityCache(actionId, entities.get(0));
                return entities.get(0);
            }
        }
        // Last attempt. Also check the last dimension from the meecreep
        Level w = LevelTools.getWorld(dimension);
        if (w == null)
            return null;
        entities = ((ServerLevel) w).getEntities(Registration.CREEP.get(), input -> input != null && input.getActionId() == actionId && !input.isRemoved());
        if (!entities.isEmpty()) {
            updateEntityCache(actionId, entities.get(0));
            return entities.get(0);
        }

        return null;
    }

    public void tick() {
        save();
        List<ActionOptions> newlist = new ArrayList<>();
        Map<Integer, ActionOptions> newmap = new HashMap<>();
        for (ActionOptions option : options) {
            EntityMeeCreeps meeCreep = findMeeCreep(LevelTools.overworld(), option.getActionId(), option.getDimension());
            boolean keep = true;

            Level world = meeCreep == null ? LevelTools.getWorld(option.getDimension()) : meeCreep.level();
            BlockPos meeCreepPos = meeCreep == null ? option.getTargetPos() : meeCreep.blockPosition();
            if (world != null && world.hasChunkAt(meeCreepPos)) {
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

    private void dropRemainingDrops(ActionOptions option, Level world) {
        List<Pair<BlockPos, ItemStack>> drops = option.getDrops();
        if (!drops.isEmpty()) {
            for (Pair<BlockPos, ItemStack> pair : drops) {
                ItemEntity entityItem = new ItemEntity(world, 0, 0, 0, ItemStack.EMPTY);
                entityItem.setItem(pair.getValue());
                BlockPos pos = pair.getKey();
                entityItem.moveTo(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                world.addFreshEntity(entityItem);
            }
        }
    }

    private void stayWithPlayer(ActionOptions option, EntityMeeCreeps meeCreep) {
        // We check here if the MeeCreep wants to follow the player
        // and if so we do the teleport here
        Player player = option.getPlayer();
        if (player != null) {
            if (meeCreep.getHelper() != null) {
                IActionWorker worker = meeCreep.getHelper().getWorker();
                if (worker.needsToFollowPlayer()) {
                    if (isDifferentDimension(player, meeCreep) || isTooFar(player, meeCreep)) {
                        // Wrong dimension. Teleport to the player
                        meeCreep.cancelJob();
                        BlockPos p = WorkerHelper.findSuitablePositionNearPlayer(meeCreep, player, 4.0);
                        meeCreep = (EntityMeeCreeps) EntityTeleportation.teleportEntity(meeCreep, player.level(), p.getX() + .5, p.getY(), p.getZ() + .5, Direction.NORTH);
                        updateEntityCache(option.getActionId(), meeCreep);
                        option.setDimension(player.level().dimension());
                    }
                }
            }
        }
    }

    private boolean isDifferentDimension(Player player, EntityMeeCreeps meeCreep) {
        return player.level().dimension() != meeCreep.level().dimension();
    }

    private boolean isTooFar(Player player, EntityMeeCreeps meeCreep) {
        return player.position().distanceToSqr(meeCreep.position()) > 60 * 60;
    }

    public void readFromNBT(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = nbt.getList("actions", Tag.TAG_COMPOUND);
        options = new ArrayList<>();
        optionMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            ActionOptions opt = new ActionOptions(list.getCompound(i), registries);
            options.add(opt);
            optionMap.put(opt.getActionId(), opt);
        }
        lastId = nbt.getInt("lastId");
    }

    @Override
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (ActionOptions option : options) {
            CompoundTag tc = new CompoundTag();
            option.writeToNBT(tc, registries);
            list.add(tc);
        }
        compound.put("actions", list);
        compound.putInt("lastId", lastId);
        return compound;
    }
}
