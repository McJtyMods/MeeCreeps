package mcjty.meecreeps.actions;

public class MeeCreepActionType {
    private final String id;

    public MeeCreepActionType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeeCreepActionType)) return false;

        MeeCreepActionType that = (MeeCreepActionType) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
