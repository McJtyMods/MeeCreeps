package mcjty.meecreeps.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;
import java.util.Random;

public class RenderMeeCreeps extends RenderLiving<EntityMeeCreeps> {

    private ResourceLocation mobTexture = new ResourceLocation("meecreeps:textures/entity/meecreeps.png");

    private static Random rand = new Random();

    public static final Factory FACTORY = new Factory();

    public RenderMeeCreeps(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new MeeCreepsModel(), 0.5F);
        this.addLayer(new LayerRenderHeldBlock(this));
    }

    @Override
    public void doRender(EntityMeeCreeps entity, double x, double y, double z, float entityYaw, float partialTicks) {
        IBlockState iblockstate = entity.getHeldBlockState();
        MeeCreepsModel model = (MeeCreepsModel) this.getMainModel();
        model.isCarrying = iblockstate != null;

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
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
