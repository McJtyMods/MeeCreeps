package mcjty.meecreeps.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class HeldCubeBlock extends Block {
    public HeldCubeBlock() {
        super(BlockBehaviour.Properties.of().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("meecreeps", "creepcube"))).strength(2));
    }
}
