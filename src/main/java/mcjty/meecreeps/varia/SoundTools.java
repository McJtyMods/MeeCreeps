package mcjty.meecreeps.varia;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class SoundTools {
    private SoundTools() {}
    public static void playSound(Level level, SoundEvent sound, double x, double y, double z, double volume, double pitch) {
        level.playSound(null, x, y, z, sound, SoundSource.NEUTRAL, (float) volume, (float) pitch);
    }
}
