package mcjty.meecreeps.varia;

import com.mojang.authlib.GameProfile;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class GeneralTools {

    private static FakePlayer harvester = null;

    public static FakePlayer getHarvester() {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(DimensionManager.getWorld(0), new GameProfile(new UUID(656, 123), "meecreps"));
        }
        return harvester;
    }
}
