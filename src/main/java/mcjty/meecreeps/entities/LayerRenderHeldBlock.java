package mcjty.meecreeps.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.client.model.data.EmptyModelData;

public class LayerRenderHeldBlock extends LayerRenderer<EntityMeeCreeps, MeeCreepsModel<EntityMeeCreeps>> {
    private final RenderMeeCreeps endermanRenderer;

    public LayerRenderHeldBlock(RenderMeeCreeps endermanRendererIn) {
        super(endermanRendererIn);
        this.endermanRenderer = endermanRendererIn;
    }

//
//    @Override
//    public boolean shouldCombineTextures() {
//        return false;
//    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, EntityMeeCreeps entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState BlockState = entitylivingbaseIn.getHeldBlockState();

        if (BlockState != null) {
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0.0F, 0.6875F, -0.75F);
            GlStateManager.rotatef(20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translatef(0.25F, 0.1875F, 0.25F);
            float f = 0.5F;
            GlStateManager.scalef(-0.5F, -0.5F, 0.5F);
            float i = entitylivingbaseIn.getBrightness();
            float j = i % 65536;
            float k = i / 65536;
            // todo: find alternaitve
//            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
            blockrendererdispatcher.renderBlock(BlockState, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
    }
}