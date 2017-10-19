package mcjty.meecreeps.proxy;

import mcjty.meecreeps.gui.GuiMeeCreeps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiProxy implements IGuiHandler {

    public static final int GUI_MEECREEP_QUESTION = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof TestContainerTileEntity) {
//            return new TestContainer(player.inventory, (TestContainerTileEntity) te);
//        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == GUI_MEECREEP_QUESTION) {
            return new GuiMeeCreeps();
        }
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof TestContainerTileEntity) {
//            TestContainerTileEntity containerTileEntity = (TestContainerTileEntity) te;
//            return new TestContainerGui(containerTileEntity, new TestContainer(player.inventory, containerTileEntity));
//        }
        return null;
    }
}
