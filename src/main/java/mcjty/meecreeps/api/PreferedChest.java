package mcjty.meecreeps.api;

public enum PreferedChest {
    TARGET,                 // Use the target (block on which the meecreeps was summoned) as a chest to put stuff away (if inventory)
    FIND_MATCHING_INVENTORY,// Find a chest in action box that matches the meecreeps inventory most
    LAST_CHEST,             // Use a dedicated chest that was previously used to get items from
    MARKED,                 // A marked chest (ItemFrame with creep cube in it)
}
