package mcjty.meecreeps.actions;

import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void createActionOptions(World world, BlockPos pos, EntityPlayer player) {
        List<MeeCreepActionType> types = new ArrayList<>();
        types.add(MeeCreepActionType.ACTION_HARVEST);
        types.add(MeeCreepActionType.ACTION_PICKUP_ITEMS);
        types.add(MeeCreepActionType.ACTION_PLACE_TORCHES);
        int actionId = newId();
        ActionOptions opt = new ActionOptions(types, pos, world.provider.getDimension(), player.getUniqueID(), actionId);
        options.add(opt);
        optionMap.put(actionId, opt);
        save();
    }

    public void performAction(ActionOptions option, MeeCreepActionType type) {
        System.out.println("ServerActionManager.performAction: " + type.getDescription());
        option = getOptions(option.getActionId());
        if (option != null) {
            option.setStage(Stage.WORKING);
        }
    }

    public void cancelAction(ActionOptions option) {
        System.out.println("ServerActionManager.cancelAction");
        option = getOptions(option.getActionId());
        if (option != null) {
            option.setStage(Stage.DONE);
        }
    }


    public void tick() {
        save();
        List<ActionOptions> newlist = new ArrayList<>();
        Map<Integer, ActionOptions> newmap = new HashMap<>();
        for (ActionOptions option : options) {
            World world = DimensionManager.getWorld(option.getDimension());
            boolean keep = true;
            if (world != null) {
                if (!option.tick(world)) {
                    keep = false;
                }
            }
            if (keep) {
                newlist.add(option);
                newmap.put(option.getActionId(), option);
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
