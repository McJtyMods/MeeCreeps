package mcjty.meecreeps.proxy;

import mcjty.meecreeps.gui.GuiBalloon;
import mcjty.meecreeps.gui.GuiMeeCreeps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

    public static final int GUI_MEECREEP_QUESTION = 1;
    public static final int GUI_MEECREEP_DISMISS = 2;
    public static final int GUI_MEECREEP_BALLOON = 3;

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
        } else if (id == GUI_MEECREEP_BALLOON) {
            return new GuiBalloon();
        }
        return null;
    }
}
