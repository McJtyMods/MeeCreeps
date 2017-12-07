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
    public boolean spawnMeeCreep(String id, String furtherQuestionId, World world, BlockPos targetPos, EnumFacing targetSide, @Nullable EntityPlayerMP player, boolean doSound) {
        ServerActionManager manager = ServerActionManager.getManager();
        int actionId = manager.createActionOptions(world, targetPos, targetSide, player);
        ActionOptions.spawn(world, targetPos, targetSide, actionId, doSound);
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
        registerActionFactory("meecreeps.make_house", "message.meecreeps.action.make_house", new MakeHouseActionFactory());
        registerActionFactory("meecreeps.make_platform", "message.meecreeps.action.make_platform", new MakePlatformActionFactory());
        registerActionFactory("meecreeps.flatten_area", "message.meecreeps.action.flatten_area", new FlattenAreaActionFactory());
        registerActionFactory("meecreeps.chop_tree", "message.meecreeps.action.chop_tree", new ChopTreeActionFactory());
        registerActionFactory("meecreeps.chop_tree_collect", "message.meecreeps.action.chop_tree_collect", new ChopTreeAndCollectActionFactory());
        registerActionFactory("meecreeps.dig_down", "message.meecreeps.action.dig_down", new DigdownActionFactory());
        registerActionFactory("meecreeps.dig_down_stairs", "message.meecreeps.action.dig_down_stairs", new DigdownStairsActionFactory());
        registerActionFactory("meecreeps.mine_ores", "message.meecreeps.action.mine_ores", new MineOresActionFactory());
        registerActionFactory("meecreeps.dig_tunnel", "message.meecreeps.action.dig_tunnel", new DigTunnelActionFactory());
        registerActionFactory("meecreeps.harvest_replant", "message.meecreeps.action.harvest_replant", new HarvestReplantActionFactory());
        registerActionFactory("meecreeps.harvest", "message.meecreeps.action.harvest", new HarvestActionFactory());
        registerActionFactory("meecreeps.torches", "message.meecreeps.action.torches", new LightupActionFactory());
        registerActionFactory("meecreeps.follow_and_lightup", "message.meecreeps.action.follow_and_lightup", new FollowAndLightupActionFactory());
        registerActionFactory("meecreeps.pickup", "message.meecreeps.action.pickup", new PickupActionFactory());
        registerActionFactory("meecreeps.follow_and_pickup", "message.meecreeps.action.follow_and_pickup", new FollowAndPickupActionFactory());
        registerActionFactory("meecreeps.move_stuff", "message.meecreeps.action.move_stuff", new MoveStuffActionFactory());
        registerActionFactory("meecreeps.idle", "message.meecreeps.action.idle", new IdleActionFactory());
        registerActionFactory("meecreeps.angry", "message.meecreeps.action.angry", new AngryActionFactory());
    }

    public MeeCreepsApi() {
    }
}
