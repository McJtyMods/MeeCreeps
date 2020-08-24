package mcjty.meecreeps.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class PortalBlock extends Block {

    public PortalBlock() {
        super(Properties.create(Material.IRON).doesNotBlockMovement().notSolid());
//        setUnlocalizedName(MeeCreeps.MODID + ".portalblock");
//        setRegistryName("portalblock");
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }


//    @SideOnly(Side.CLIENT)
//    @Override
//    public boolean shouldSideBeRendered(BlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
//        return false;
//    }


    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

// todo: add back the TES
//    public void initModel() {
//        McJtyLib.proxy.initStandardItemModel(this);
//        PortalTESR.register();
//    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PortalTileEntity();
    }
}
