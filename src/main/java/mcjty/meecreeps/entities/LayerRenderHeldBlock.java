package mcjty.meecreeps.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class LayerRenderHeldBlock extends RenderLayer<MeeCreepsRenderState, MeeCreepsModel> {

    public LayerRenderHeldBlock(RenderLayerParent<MeeCreepsRenderState, MeeCreepsModel> parent) {
        super(parent);
    }

    public void submit(PoseStack pose, SubmitNodeCollector collector, int light, MeeCreepsRenderState state, float yaw, float pitch) {
        if (state.carriedBlock.isEmpty()) return;
        pose.pushPose();
        pose.translate(0, .5, -.5);
        pose.mulPose(Axis.XP.rotationDegrees(20));
        pose.scale(.5F, -.5F, .5F);
        pose.translate(-.5, 0, -.5);
        state.carriedBlock.submit(pose, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
    }
}
