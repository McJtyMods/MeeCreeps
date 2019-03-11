package mcjty.meecreeps.setup;

import mcjty.meecreeps.gui.GuiAskName;
import mcjty.meecreeps.gui.GuiMeeCreeps;
import mcjty.meecreeps.gui.GuiWheel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

    public static final int GUI_MEECREEP_QUESTION = 1;
    public static final int GUI_MEECREEP_DISMISS = 2;
    public static final int GUI_WHEEL = 3;
    public static final int GUI_ASKNAME = 4;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_MEECREEP_QUESTION) {
            return new GuiMeeCreeps(id);
        } else if (id == GUI_MEECREEP_DISMISS) {
            return new GuiMeeCreeps(id);
        } else if (id == GUI_WHEEL) {
            return new GuiWheel();
        } else if (id == GUI_ASKNAME) {
            return new GuiAskName();
        }
        return null;
    }
}
