package mcjty.meecreeps.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class RenderProjectile extends EntityRenderer<ProjectileItemEntity> {

    public RenderProjectile(EntityRendererManager renderManager) {
        super(renderManager);
    }

    // todo: move to matric system
    @Override
    public void render(ProjectileItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
// todo come back to this one

//        RenderSystem.pushMatrix();
//        RenderSystem.translatef((float) x, (float) y, (float) z);
//        RenderSystem.enableRescaleNormal();
//        RenderSystem.rotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
//        RenderSystem.rotatef((this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * this.renderManager., 1.0F, 0.0F, 0.0F);
//        RenderSystem.rotatef(180.0F, 0.0F, 1.0F, 0.0F);
//        RenderSystem.disableLighting();
//
//        Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
//        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//
//        if (this.renderOutlines) {
//            RenderSystem.enableColorMaterial();
//            RenderSystem.enableOutlineMode(this.getTeamColor(entity));
//        }
//
//        this.itemRenderer.renderItem(new ItemStack(ModItems.PROJECTILE_ITEM.get()), ItemCameraTransforms.TransformType.GROUND);
//
//        if (this.renderOutlines) {
//            RenderSystem.disableOutlineMode();
//            RenderSystem.disableColorMaterial();
//        }
//
//        RenderSystem.enableLighting();
//        RenderSystem.disableRescaleNormal();
//        RenderSystem.popMatrix();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    public ResourceLocation getEntityTexture(ProjectileItemEntity entity) {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }
}