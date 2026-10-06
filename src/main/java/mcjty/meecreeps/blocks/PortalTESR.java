package mcjty.meecreeps.blocks;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;


public class PortalTESR implements BlockEntityRenderer<PortalTileEntity, PortalTESR.State> {
    public PortalTESR(BlockEntityRendererProvider.Context context) {
    }

    public static class State extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        Direction side;
        float scale, time;
    }

    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(PortalTileEntity tile, State state, float partial,
            net.minecraft.world.phys.Vec3 camera, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partial, camera, overlay);
        state.side = tile.getPortalSide();
        state.scale = Math.min(1, Math.max(0, Math.min(tile.getStart() + partial, tile.getTimeout() - partial) / 10F));
        state.time = tile.getLevel() == null ? 0 : tile.getLevel().getGameTime() + partial;
    }

    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        Direction side = state.side;
        if (side == null) return;
        pose.pushPose();
        // The portal block is outside the selected face; its supporting block
        // lies opposite portalSide. Keep the surface just clear of that face.
        pose.translate(.5 - side.getStepX() * .49, .5 - side.getStepY() * .49, .5 - side.getStepZ() * .49);
        if (side.getAxis().isHorizontal()) {
            pose.mulPose(Axis.YP.rotationDegrees(-side.toYRot()));
        } else {
            pose.mulPose(Axis.XP.rotationDegrees(side == Direction.UP ? 90 : -90));
        }
        pose.scale(state.scale, state.scale, state.scale);
        pose.mulPose(Axis.ZP.rotationDegrees(state.time * 2));
        // A square quad keeps the circular texture round throughout rotation.
        float halfHeight = 1F;
        // Vanilla's emissive shader still applies directional diffuse shading.
        collector.submitCustomGeometry(pose, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(Identifier.fromNamespaceAndPath("meecreeps", "textures/effects/portal.png")), (snapshot, v) -> {
            var matrix = snapshot.pose();
            v.addVertex(matrix, -1, -halfHeight, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(0).setLight(15728880).setNormal(snapshot, 0, 0, 1);
            v.addVertex(matrix, 1, -halfHeight, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(0).setLight(15728880).setNormal(snapshot, 0, 0, 1);
            v.addVertex(matrix, 1, halfHeight, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(0).setLight(15728880).setNormal(snapshot, 0, 0, 1);
            v.addVertex(matrix, -1, halfHeight, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(0).setLight(15728880).setNormal(snapshot, 0, 0, 1);
        });
        pose.popPose();
    }

    public boolean shouldRenderOffScreen() {
        return true;
    }
}
