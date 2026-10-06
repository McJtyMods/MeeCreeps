package mcjty.meecreeps.items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
/** Items whose action takes priority over the clicked block's interaction. */
public interface FirstUseItem {
    InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
