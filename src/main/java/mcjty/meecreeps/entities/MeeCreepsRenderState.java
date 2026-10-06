package mcjty.meecreeps.entities;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;

public class MeeCreepsRenderState extends LivingEntityRenderState {
    public int face, hair;
    public final BlockModelRenderState carriedBlock = new BlockModelRenderState();
}
