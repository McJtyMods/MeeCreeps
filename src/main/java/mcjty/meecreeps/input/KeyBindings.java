package mcjty.meecreeps.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class KeyBindings {
    public static final KeyMapping REPEAT = new KeyMapping("key.meecreeps.repeat", InputConstants.Type.KEYBOARD, InputConstants.KEY_B, KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "meecreeps")));
}
