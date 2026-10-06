package mcjty.meecreeps.network;

import net.minecraft.network.FriendlyByteBuf;

public final class NetworkTools {
    private NetworkTools() {}
    public static String readStringUTF8(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUtf() : null;
    }
    public static void writeStringUTF8(FriendlyByteBuf buf, String value) {
        buf.writeBoolean(value != null);
        if (value != null) buf.writeUtf(value);
    }
}
