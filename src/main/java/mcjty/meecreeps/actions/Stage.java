package mcjty.meecreeps.actions;

import java.util.HashMap;
import java.util.Map;

public enum Stage {
    WAITING_FOR_SPAWN("wait", 20),
    OPENING_GUI("gui", 20),
    WAITING_FOR_PLAYER_INPUT("input", 20),
    WORKING("working", 20*120),
    TIME_IS_UP("timeup", 20),
    TASK_IS_DONE("taskDone", 20),
    DONE("done", 20);

    private static final Map<String, Stage> TYPE_MAP = new HashMap<>();

    static {
        for (Stage type : values()) {
            TYPE_MAP.put(type.getCode(), type);
        }
    }

    private final String code;
    private final int timeout;

    Stage(String code, int timeout) {
        this.code = code;
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getCode() {
        return code;
    }

    public static Stage getByCode(String code) {
        return TYPE_MAP.get(code);
    }
}
