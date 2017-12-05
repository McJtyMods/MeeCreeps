package mcjty.meecreeps.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding repeatLastMessage;

    public static void init() {
        repeatLastMessage = new KeyBinding("key.last_message", KeyConflictContext.IN_GAME, Keyboard.KEY_R, "key.categories.meecreeps");
        ClientRegistry.registerKeyBinding(repeatLastMessage);
    }
}
