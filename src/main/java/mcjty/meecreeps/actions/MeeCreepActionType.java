package mcjty.meecreeps.actions;

import mcjty.meecreeps.actions.factories.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MeeCreepActionType {
    ACTION_CHOP_TREE("chop_tree", "Chop this tree", new ChopTreeActionFactory()),
    ACTION_HARVEST_REPLANT("harvest_replant", "Harvest and replant those crops", new HarvestReplantActionFactory()),
    ACTION_HARVEST("harvest", "Harvest those crops", new HarvestActionFactory()),
    ACTION_PLACE_TORCHES("torches", "Light up the area", new LightupActionFactory()),
    ACTION_PICKUP_ITEMS("pickup", "Pickup items", new PickupActionFactory());

    public static final MeeCreepActionType[] VALUES;
    private static final Map<String, MeeCreepActionType> TYPE_MAP = new HashMap<>();

    private final String code;
    private final String description;
    private final IActionFactory actionFactory;

    static {
        List<MeeCreepActionType> v = new ArrayList<>();
        for (MeeCreepActionType type : values()) {
            v.add(type);
            TYPE_MAP.put(type.getCode(), type);
        }
        VALUES = v.toArray(new MeeCreepActionType[v.size()]);
    }

    MeeCreepActionType(String code, String description, IActionFactory actionFactory) {
        this.code = code;
        this.description = description;
        this.actionFactory = actionFactory;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public IActionFactory getActionFactory() {
        return actionFactory;
    }

    public static MeeCreepActionType getByCode(String code) {
        return TYPE_MAP.get(code);
    }
}
