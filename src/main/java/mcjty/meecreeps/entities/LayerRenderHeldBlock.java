package mcjty.meecreeps.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRenderHeldBlock implements LayerRenderer<EntityMeeCreeps> {
    private final RenderMeeCreeps endermanRenderer;

    public LayerRenderHeldBlock(RenderMeeCreeps endermanRendererIn) {
        this.endermanRenderer = endermanRendererIn;
    }

    @Override
    public void doRenderLayer(EntityMeeCreeps entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        IBlockState iblockstate = entitylivingbaseIn.getHeldBlockState();

        if (iblockstate != null) {
            BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.6875F, -0.75F);
            GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.25F, 0.1875F, 0.25F);
            float f = 0.5F;
            GlStateManager.scale(-0.5F, -0.5F, 0.5F);
            int i = entitylivingbaseIn.getBrightnessForRender();
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.endermanRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            blockrendererdispatcher.renderBlockBrightness(iblockstate, 1.0F);
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}