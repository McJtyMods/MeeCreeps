package mcjty.meecreeps.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class HeldCubeBlock extends Block {
    public HeldCubeBlock() {
        super(BlockBehaviour.Properties.of().strength(2));
    }
}
