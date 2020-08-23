package mcjty.meecreeps.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding repeatLastMessage;

    public static void init() {
        repeatLastMessage = new KeyBinding("key.last_message", KeyConflictContext.IN_GAME, InputMappings.Type.SCANCODE, GLFW.GLFW_KEY_R, "key.categories.meecreeps");
        ClientRegistry.registerKeyBinding(repeatLastMessage);
    }
}
