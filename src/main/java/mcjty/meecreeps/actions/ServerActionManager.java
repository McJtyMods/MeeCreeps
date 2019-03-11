package mcjty.meecreeps.actions;

import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.TeleportationTools;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IActionWorker;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.items.CreepCubeItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
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

    @Override
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
    public void clearOptions(ICommandSender sender, @Nullable EntityPlayer player) {
        if (player == null) {
            sender.sendMessage(new TextComponentString("Cleared " + options.size() + " active operations"));
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
            sender.sendMessage(new TextComponentString("Cleared " + cnt + " active operations"));
        }
        save();

        if (player == null) {
            Integer[] iDs = DimensionManager.getStaticDimensionIDs();
            int cnt = 0;
            for (Integer id : iDs) {
                World w = TeleportationTools.getWorldForDimension(id);
                List<EntityMeeCreeps> entities = w.getEntities(EntityMeeCreeps.class, input -> true);
                for (EntityMeeCreeps entity : entities) {
                    entity.setDead();
                    cnt++;
                }
            }
            sender.sendMessage(new TextComponentString("Additionally killed " + cnt + " MeeCreeps"));
        }
    }

    public void listOptions(ICommandSender sender) {
        for (Map.Entry<Integer, ActionOptions> entry : optionMap.entrySet()) {
            ActionOptions options = entry.getValue();
            Stage stage = options.getStage();
            MeeCreepActionType task = options.getTask();
            EntityMeeCreeps entity = findMeeCreep(sender.getEntityWorld(), entry.getKey(), options.getDimension());
            String name = entity == null ? "<none>" : entity.getUniqueID().toString();
            sender.sendMessage(new TextComponentString("Action " + entry.getKey() + ", Task " + task.getId() + ", Stage " + stage + ", Entity " + name));
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

    public int countMeeCreeps(EntityPlayer player) {
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
        return getData(ServerActionManager.class, NAME);
    }

    public int createActionOptions(World world, BlockPos pos, EnumFacing side, @Nullable EntityPlayer player) {
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
        ActionOptions opt = new ActionOptions(types, maybeTypes, pos, side, world.provider.getDimension(), player == null ? null : player.getUniqueID(), actionId);
        options.add(opt);
        optionMap.put(actionId, opt);
        save();
        return actionId;
    }

    private static Random random = new Random();

    public void performAction(@Nullable EntityPlayerMP player, int id, MeeCreepActionType type, @Nullable String furtherQuestionId) {
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
                    SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(MeeCreeps.MODID, snd));
                    SoundTools.playSound(player.getEntityWorld(), sound, player.posX, player.posY, player.posZ, ConfigSetup.meeCreepVolume.get(), 1);
                }
            }
        }
    }

    public void cancelAction(EntityPlayerMP player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            option.setStage(Stage.DONE);
            option.setPaused(false);
        }
    }

    public void resumeAction(EntityPlayerMP player, int id) {
        ActionOptions option = getOptions(id);
        if (option != null) {
            option.setPaused(false);
        }
    }

    // The dimension parameter is the dimension where the meecreep was last seen
    private EntityMeeCreeps findMeeCreep(World world, int actionId, int dimension) {
        EntityMeeCreeps cachedEntity = getCachedEntity(actionId);
        if (cachedEntity != null && !cachedEntity.isDead) {
            return cachedEntity;
        }
        List<EntityMeeCreeps> entities = world.getEntities(EntityMeeCreeps.class, input -> input != null && input.getActionId() == actionId && !input.isDead);
        if (!entities.isEmpty()) {
            updateEntityCache(actionId, entities.get(0));
            return entities.get(0);
        }
        // Lets try to find the entity in other dimensions that are still loaded
        for (WorldServer w : DimensionManager.getWorlds()) {
            entities = w.getEntities(EntityMeeCreeps.class, input -> input != null && input.getActionId() == actionId && !input.isDead);
            if (!entities.isEmpty()) {
                updateEntityCache(actionId, entities.get(0));
                return entities.get(0);
            }
        }
        // Last attempt. Also check the last dimension from the meecreep
        World w = TeleportationTools.getWorldForDimension(dimension);
        entities = w.getEntities(EntityMeeCreeps.class, input -> input != null && input.getActionId() == actionId && !input.isDead);
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
            EntityMeeCreeps meeCreep = findMeeCreep(DimensionManager.getWorld(0), option.getActionId(), option.getDimension());
            boolean keep = true;

            World world = meeCreep == null ? DimensionManager.getWorld(option.getDimension()) : meeCreep.getEntityWorld();
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
                EntityItem entityItem = new EntityItem(world);
                entityItem.setItem(pair.getValue());
                BlockPos pos = pair.getKey();
                entityItem.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
                world.spawnEntity(entityItem);
            }
        }
    }

    private void stayWithPlayer(ActionOptions option, EntityMeeCreeps meeCreep) {
        // We check here if the MeeCreep wants to follow the player
        // and if so we do the teleport here
        EntityPlayer player = option.getPlayer();
        if (player != null) {
            if (meeCreep.getHelper() != null) {
                IActionWorker worker = meeCreep.getHelper().getWorker();
                if (worker.needsToFollowPlayer()) {
                    if (isDifferentDimension(player, meeCreep) || isTooFar(player, meeCreep)) {
                        // Wrong dimension. Teleport to the player
                        meeCreep.cancelJob();
                        BlockPos p = WorkerHelper.findSuitablePositionNearPlayer(meeCreep, player, 4.0);
                        meeCreep = (EntityMeeCreeps) TeleportationTools.teleportEntity(meeCreep, player.getEntityWorld(), p.getX() + .5, p.getY(), p.getZ() + .5, EnumFacing.NORTH);
                        updateEntityCache(option.getActionId(), meeCreep);
                        option.setDimension(player.getEntityWorld().provider.getDimension());
                    }
                }
            }
        }
    }

    private boolean isDifferentDimension(EntityPlayer player, EntityMeeCreeps meeCreep) {
        return player.getEntityWorld().provider.getDimension() != meeCreep.getEntityWorld().provider.getDimension();
    }

    private boolean isTooFar(EntityPlayer player, EntityMeeCreeps meeCreep) {
        return player.getPositionVector().squareDistanceTo(meeCreep.getPositionVector()) > 60*60;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("actions", Constants.NBT.TAG_COMPOUND);
        options = new ArrayList<>();
        optionMap = new HashMap<>();
        for (int i = 0; i < list.tagCount(); i++) {
            ActionOptions opt = new ActionOptions(list.getCompoundTagAt(i));
            options.add(opt);
            optionMap.put(opt.getActionId(), opt);
        }
        lastId = nbt.getInteger("lastId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (ActionOptions option : options) {
            NBTTagCompound tc = new NBTTagCompound();
            option.writeToNBT(tc);
            list.appendTag(tc);
        }
        compound.setTag("actions", list);
        compound.setInteger("lastId", lastId);
        return compound;
    }
}
