package mcjty.meecreeps;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.MeeCreepActionType;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.factories.*;
import mcjty.meecreeps.api.IActionFactory;
import mcjty.meecreeps.api.IMeeCreepsApi;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeeCreepsApi implements IMeeCreepsApi {

    public static class Factory {
        private final String id;
        private final String message;
        private final IActionFactory factory;

        public Factory(String id, String message, IActionFactory factory) {
            this.id = id;
            this.message = message;
            this.factory = factory;
        }

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public IActionFactory getFactory() {
            return factory;
        }
    }


    private List<Factory> factories = new ArrayList<>();
    private Map<MeeCreepActionType, Factory> factoryMap = new HashMap<>();

    @Override
    public void registerActionFactory(String id, String message, IActionFactory factory) {
        Factory fact = new Factory(id, message, factory);
        factories.add(fact);
        factoryMap.put(new MeeCreepActionType(id), fact);
    }

    @Override
    public boolean spawnMeeCreep(String id, String furtherQuestionId, World world, BlockPos targetPos, EnumFacing targetSide, @Nullable EntityPlayerMP player) {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = manager.createActionOptions(world, targetPos, targetSide, player);
        ActionOptions.spawn(world, targetPos, targetSide, actionId);
        manager.performAction(player, actionId, new MeeCreepActionType(id), furtherQuestionId);
        return true;
    }

    public List<Factory> getFactories() {
        return factories;
    }

    public Factory getFactory(MeeCreepActionType id) {
        return factoryMap.get(id);
    }

    public void registerFactories() {
        registerActionFactory("meecreeps.make_house", "Build a house", new MakeHouseActionFactory());
        registerActionFactory("meecreeps.chop_tree", "Chop this tree", new ChopTreeActionFactory());
        registerActionFactory("meecreeps.dig_down", "Dig straight down to bedrock", new DigdownActionFactory());
        registerActionFactory("meecreeps.dig_down_stairs", "Dig down to bedrock using stairs", new DigdownStairsActionFactory());
        registerActionFactory("meecreeps.mine_ores", "Find and mine all nearby ores", new MineOresActionFactory());
        registerActionFactory("meecreeps.chop_tree_collect", "Chop this tree and get the drops", new ChopTreeAndCollectActionFactory());
        registerActionFactory("meecreeps.harvest_replant", "Harvest and replant those crops", new HarvestReplantActionFactory());
        registerActionFactory("meecreeps.harvest", "Harvest those crops", new HarvestActionFactory());
        registerActionFactory("meecreeps.torches", "Light up the area", new LightupActionFactory());
        registerActionFactory("meecreeps.pickup", "Pickup items", new PickupActionFactory());
    }

    public MeeCreepsApi() {
    }
}
