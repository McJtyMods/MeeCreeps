package mcjty.meecreeps.actions;

import java.util.ArrayList;
import java.util.List;

public enum MeeCreepActionType {
    ACTION_HARVEST("Harvest those crops"),
    ACTION_PLACE_TORCHES("Light up the area"),
    ACTION_PICKUP_ITEMS("Pickup items");

    public static final MeeCreepActionType[] VALUES;

    private final String description;

    static {
        List<MeeCreepActionType> v = new ArrayList<>();
        for (MeeCreepActionType type : values()) {
            v.add(type);
        }
        VALUES = v.toArray(new MeeCreepActionType[v.size()]);
    }

    MeeCreepActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
