package mcjty.meecreeps.actions;

import java.util.HashMap;
import java.util.Map;

enum Stage {
    WAITING_FOR_SPAWN("wait"),
    OPENING_GUI("gui"),
    WAITING_FOR_PLAYER_INPUT("input"),
    WORKING("working"),
    DONE("done");

    private static final Map<String, Stage> TYPE_MAP = new HashMap<>();

    static {
        for (Stage type : values()) {
            TYPE_MAP.put(type.getCode(), type);
        }
    }

    private final String code;

    Stage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Stage getByCode(String code) {
        return TYPE_MAP.get(code);
    }
}
