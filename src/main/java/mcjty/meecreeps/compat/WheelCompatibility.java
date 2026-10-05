package mcjty.meecreeps.compat;

import mcjty.intwheel.api.*;
import mcjty.meecreeps.items.PortalGunItem;
import mcjty.meecreeps.setup.ClientSetup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.*;

import java.util.Set;
import java.util.function.Function;

public class WheelCompatibility implements Function<IInteractionWheel, Void> {
    public Void apply(IInteractionWheel wheel) {
        wheel.registerProvider(new IWheelActionProvider() {
            public String getID() {
                return "meecreeps:portalgun";
            }

            public void updateWheelActions(Set<String> actions, Player player, Level world, BlockPos pos) {
                if (pos != null && !PortalGunItem.getGun(player).isEmpty())
                    actions.add("meecreeps.destinations");
            }
        });
        wheel.getRegistry().register(new IWheelAction() {
            public String getId() {
                return "meecreeps.destinations";
            }

            public WheelActionElement createElement() {
                return new WheelActionElement(getId()).description("Portal destinations", null).texture("meecreeps:textures/gui/wheel_hilight.png", 0, 0, 0, 0, 256, 256);
            }

            public boolean performClient(Player player, Level world, BlockPos pos, boolean extended) {
                if (pos != null) {
                    var hit = player.pick(6, 0, false);
                    Direction side = hit instanceof net.minecraft.world.phys.BlockHitResult block ? block.getDirection() : Direction.UP;
                    ClientSetup.openWheel(pos, side);
                }
                return false;
            }

            public void performServer(Player player, Level world, BlockPos pos, boolean extended) {
            }
        });
        return null;
    }
}
