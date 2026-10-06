package mcjty.meecreeps.varia;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class ItemSerialization {
    private ItemSerialization() {}
    public static Tag save(HolderLookup.Provider registries, ItemStack stack) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }
    public static ItemStack load(HolderLookup.Provider registries, Tag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }
}
