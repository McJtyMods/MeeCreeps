package mcjty.meecreeps.actions;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.MeeCreepsApi;
import mcjty.meecreeps.config.Config;
import mcjty.meecreeps.varia.SoundTools;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ServerActionManager extends WorldSavedData {

    public static final String NAME = "MeeCreepsData";
    private static ServerActionManager instance = null;

    private List<ActionOptions> options = new ArrayList<>();
    private Map<Integer, ActionOptions> optionMap = new HashMap<>();
    private int lastId = 0;

    public ServerActionManager(String name) {
        super(name);
    }

    public void save() {
        World world = DimensionManager.getWorld(0);
        world.setData(NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public int newId() {
        lastId++;
        save();
        return lastId;
    }

    public ActionOptions getOptions(int id) {
        return optionMap.get(id);
    }

    @Nonnull
    public static ServerActionManager getManager() {
        if (instance != null) {
            return instance;
        }
        WorldServer world = DimensionManager.getWorld(0);
        instance = (ServerActionManager) world.loadData(ServerActionManager.class, NAME);
        if (instance == null) {
            instance = new ServerActionManager(NAME);
        }
        return instance;
    }

    public int createActionOptions(World world, BlockPos pos, EnumFacing side, @Nullable EntityPlayer player) {
        List<MeeCreepActionType> types = new ArrayList<>();
        List<MeeCreepActionType> maybeTypes = new ArrayList<>();
        for (MeeCreepsApi.Factory type : MeeCreeps.api.getFactories()) {
            if (type.getFactory().isPossible(world, pos, side)) {
                types.add(new MeeCreepActionType(type.getId()));
            } else if (type.getFactory().isPossibleSecondary(world, pos, side)) {
                maybeTypes.add(new MeeCreepActionType(type.getId()));
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
                if (Config.meeCreepVolume > 0.01f) {
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
                    SoundTools.playSound(player.getEntityWorld(), sound, player.posX, player.posY, player.posZ, Config.meeCreepVolume, 1);
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


    public void tick() {
        save();
        List<ActionOptions> newlist = new ArrayList<>();
        Map<Integer, ActionOptions> newmap = new HashMap<>();
        for (ActionOptions option : options) {
            World world = DimensionManager.getWorld(option.getDimension());
            boolean keep = true;
            if (world != null && world.isBlockLoaded(option.getTargetPos())) {
                if (!option.tick(world)) {
                    keep = false;
                }
            }
            if (keep) {
                newlist.add(option);
                newmap.put(option.getActionId(), option);
            } else {
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
        }
        options = newlist;
        optionMap = newmap;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList list = nbt.getTagList("actions", Constants.NBT.TAG_COMPOUND);
        options = new ArrayList<>();
        optionMap = new HashMap<>();
        for (int i = 0 ; i < list.tagCount() ; i++) {
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
