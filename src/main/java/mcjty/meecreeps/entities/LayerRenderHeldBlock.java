package mcjty.meecreeps.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class LayerRenderHeldBlock extends RenderLayer<EntityMeeCreeps, MeeCreepsModel> {
    private final BlockRenderDispatcher blocks;

    public LayerRenderHeldBlock(RenderLayerParent<EntityMeeCreeps, MeeCreepsModel> parent, BlockRenderDispatcher blocks) {
        super(parent);
        this.blocks = blocks;
    }

    public void render(PoseStack pose, MultiBufferSource buffers, int light, EntityMeeCreeps entity, float a, float b, float c, float d, float e, float f) {
        var state = entity.getHeldBlockState();
        if (state == null)
            return;
        pose.pushPose();
        pose.translate(0, .5, -.5);
        pose.mulPose(Axis.XP.rotationDegrees(20));
        pose.scale(.5F, -.5F, .5F);
        pose.translate(-.5, 0, -.5);
        blocks.renderSingleBlock(state, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
