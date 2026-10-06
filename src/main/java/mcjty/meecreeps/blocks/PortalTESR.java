package mcjty.meecreeps.blocks;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;

public class PortalTESR implements BlockEntityRenderer<PortalTileEntity> {
    public PortalTESR(BlockEntityRendererProvider.Context context) {
    }

    public void render(PortalTileEntity tile, float partial, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Direction side = tile.getPortalSide();
        if (side == null)
            return;
        pose.pushPose();
        // The portal block is outside the selected face; its supporting block
        // lies opposite portalSide. Keep the surface just clear of that face.
        pose.translate(.5 - side.getStepX() * .49, .5 - side.getStepY() * .49, .5 - side.getStepZ() * .49);
        if (side.getAxis().isHorizontal()) {
            pose.mulPose(Axis.YP.rotationDegrees(-side.toYRot()));
        } else {
            pose.mulPose(Axis.XP.rotationDegrees(side == Direction.UP ? 90 : -90));
        }
        float scale = Math.min(1, Math.max(0, Math.min(tile.getStart() + partial, tile.getTimeout() - partial) / 10F));
        pose.scale(scale, scale, scale);
        float time = tile.getLevel() == null ? 0 : tile.getLevel().getGameTime() + partial;
        pose.mulPose(Axis.ZP.rotationDegrees(time * 2));
        // A square quad keeps the circular texture round throughout rotation.
        float halfHeight = 1F;
        // Vanilla's emissive shader still applies directional diffuse shading.
        var v = buffers.getBuffer(NeoForgeRenderTypes.getUnlitTranslucent(ResourceLocation.fromNamespaceAndPath("meecreeps", "textures/effects/portal.png")));
        var matrix = pose.last().pose();
        var normal = pose.last().normal();
        v.addVertex(matrix, -1, -halfHeight, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 0, 1);
        v.addVertex(matrix, 1, -halfHeight, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 0, 1);
        v.addVertex(matrix, 1, halfHeight, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 0, 1);
        v.addVertex(matrix, -1, halfHeight, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(overlay).setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), 0, 0, 1);
        pose.popPose();
    }

    public boolean shouldRenderOffScreen(PortalTileEntity tile) {
        return true;
    }
}
