package mcjty.meecreeps.entities;

import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.resources.Identifier;

public class RenderMeeCreeps extends MobRenderer<EntityMeeCreeps, MeeCreepsRenderState, MeeCreepsModel> {
    private final BlockModelResolver blocks;
    public RenderMeeCreeps(EntityRendererProvider.Context context) {
        super(context, new MeeCreepsModel(context.bakeLayer(MeeCreepsModel.LAYER)), .5F);
        blocks = context.getBlockModelResolver();
        addLayer(new LayerRenderHeldBlock(this));
    }
    @Override public MeeCreepsRenderState createRenderState() { return new MeeCreepsRenderState(); }
    @Override public void extractRenderState(EntityMeeCreeps entity, MeeCreepsRenderState state, float partial) {
        super.extractRenderState(entity, state, partial);
        state.face = entity.getVariationFace();
        state.hair = entity.getVariationHair();
        var carried = entity.getHeldBlockState();
        if (carried == null) state.carriedBlock.clear();
        else blocks.update(state.carriedBlock, carried, EndermanRenderer.BLOCK_DISPLAY_CONTEXT);
    }
    @Override public Identifier getTextureLocation(MeeCreepsRenderState state) {
        return Identifier.fromNamespaceAndPath("meecreeps", "textures/entity/meecreeps.png");
    }
}
