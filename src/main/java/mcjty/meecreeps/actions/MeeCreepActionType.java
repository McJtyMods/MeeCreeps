package mcjty.meecreeps.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum MeeCreepActionType {
    ACTION_HARVEST("harvest", "Harvest those crops"),
    ACTION_PLACE_TORCHES("torches", "Light up the area"),
    ACTION_PICKUP_ITEMS("pickup", "Pickup items");

    public static final MeeCreepActionType[] VALUES;
    private static final Map<String, MeeCreepActionType> TYPE_MAP = new HashMap<>();

    private final String code;
    private final String description;

    static {
        List<MeeCreepActionType> v = new ArrayList<>();
        for (MeeCreepActionType type : values()) {
            v.add(type);
            TYPE_MAP.put(type.getCode(), type);
        }
        VALUES = v.toArray(new MeeCreepActionType[v.size()]);
    }

    MeeCreepActionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static MeeCreepActionType getByCode(String code) {
        return TYPE_MAP.get(code);
    }
}
