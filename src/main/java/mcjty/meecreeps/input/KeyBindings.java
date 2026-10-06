package mcjty.meecreeps.input;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    public static final KeyMapping REPEAT = new KeyMapping("key.meecreeps.repeat", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "meecreeps")));
}
