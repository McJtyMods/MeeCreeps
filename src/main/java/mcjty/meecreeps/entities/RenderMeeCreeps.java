package mcjty.meecreeps.entities;

import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;

public class RenderMeeCreeps extends MobRenderer<EntityMeeCreeps, MeeCreepsModel> {
    public RenderMeeCreeps(EntityRendererProvider.Context context) {
        super(context, new MeeCreepsModel(context.bakeLayer(MeeCreepsModel.LAYER)), .5F);
        addLayer(new LayerRenderHeldBlock(this, context.getBlockRenderDispatcher()));
    }

    public ResourceLocation getTextureLocation(EntityMeeCreeps entity) {
        return ResourceLocation.fromNamespaceAndPath("meecreeps", "textures/entity/meecreeps.png");
    }
}
