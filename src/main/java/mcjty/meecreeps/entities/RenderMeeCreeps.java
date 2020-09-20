package mcjty.meecreeps.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nonnull;
import java.util.Random;

// todo: 100% broke this :D
public class RenderMeeCreeps extends LivingRenderer<EntityMeeCreeps, MeeCreepsModel<EntityMeeCreeps>> {

    private ResourceLocation mobTexture = new ResourceLocation("meecreeps:textures/entity/meecreeps.png");

    private static Random rand = new Random();

//    public static final Factory FACTORY = new Factory();
    public RenderMeeCreeps(EntityRendererManager renderManager) {
        super(renderManager, new MeeCreepsModel(1f), 0.5F);
        this.addLayer(new LayerRenderHeldBlock(this));
    }

    @Override
    public void render(EntityMeeCreeps entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        BlockState BlockState = entityIn.getHeldBlockState();
        MeeCreepsModel model = this.getEntityModel();
        model.isCarrying = BlockState != null;

        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    @Nonnull
    public ResourceLocation getEntityTexture(@Nonnull EntityMeeCreeps entity) {
        return mobTexture;
    }

    public static class Factory implements IRenderFactory<EntityMeeCreeps> {

        @Override
        public EntityRenderer<? super EntityMeeCreeps> createRenderFor(EntityRendererManager manager) {
            return new RenderMeeCreeps(manager);
        }
    }

}
