package mcjty.meecreeps.entities;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;

public class RenderMeeCreeps extends RenderLiving<EntityMeeCreeps> {

    private ResourceLocation mobTexture = new ResourceLocation("meecreeps:textures/entity/meecreeps.png");

    public static final Factory FACTORY = new Factory();

    public RenderMeeCreeps(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelEnderman(0), 0.5F);
    }

    @Override
    protected void preRenderCallback(EntityMeeCreeps entitylivingbaseIn, float partialTickTime) {
        GlStateManager.scale(0.8f, 0.8f, 0.8f);
    }

    @Override
    @Nonnull
    protected ResourceLocation getEntityTexture(@Nonnull EntityMeeCreeps entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityMeeCreeps> {

        @Override
        public Render<? super EntityMeeCreeps> createRenderFor(RenderManager manager) {
            return new RenderMeeCreeps(manager);
        }

    }

}
