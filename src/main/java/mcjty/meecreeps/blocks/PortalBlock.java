package mcjty.meecreeps.blocks;

import mcjty.meecreeps.setup.Registration;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;

public class PortalBlock extends BaseEntityBlock {
    public PortalBlock() {
        super(BlockBehaviour.Properties.of().noCollission().noOcclusion().strength(-1).lightLevel(s -> 7));
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public VoxelShape getShape(BlockState s, BlockGetter w, BlockPos p, CollisionContext c) {
        return Shapes.empty();
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalTileEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState s, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, Registration.PORTAL_TILE.get(), (w, p, b, t) -> t.update());
    }
}
