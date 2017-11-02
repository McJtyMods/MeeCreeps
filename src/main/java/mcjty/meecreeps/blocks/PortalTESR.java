package mcjty.meecreeps.blocks;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class PortalTESR extends TileEntitySpecialRenderer<PortalTileEntity> {

    private static final ResourceLocation portal = new ResourceLocation(MeeCreeps.MODID, "textures/effects/portal.png");

    private static double angle = 0;

    @Override
    public void render(PortalTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        GlStateManager.pushMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate((float) x + 0.5F, (float) y + 1F, (float) z + 0.5F);
        this.bindTexture(portal);

        long time = System.currentTimeMillis();
        angle = (time / 400.0) % 360.0;
        float scale = 1.0f;
        int start = te.getStart();
        if (start < 10) {
            scale = start / 10.0f;
        } else {
            int timeout = te.getTimeout();
            if (timeout < 10) {
                scale = timeout / 10.0f;
            }
        }

        double dx = 0;
        double dz = 0;

        switch (te.getPortalSide()) {
            case DOWN:
                // @todo
                break;
            case UP:
                // Cannot happen
                break;
            case NORTH:
                dz = -.4;
                break;
            case SOUTH:
                dz = .4;
                break;
            case WEST:
                dx = -.4;
                break;
            case EAST:
                dx = .4;
                break;
        }
        // @todo offset, todo direction

        renderQuadBright(scale, angle);

        GlStateManager.popMatrix();
    }


    public static void renderQuadBright(double scale, double angle) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        double f = 1.75;
        renderFace(scale/2, scale, angle, b1, b2, buffer, f, 200);
        renderFace(scale/2, scale, -angle, b1, b2, buffer, f, 60);
        renderFace(scale/2, -scale, angle, b1, b2, buffer, f, 200);
        renderFace(scale/2, -scale, -angle, b1, b2, buffer, f, 60);


        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private static void renderFace(double scale1, double scale2, double angle, int b1, int b2, BufferBuilder buffer, double f, int alpha) {
        double u;
        double v;
        double swap;
        u = (Math.cos(angle)) / f;
        v = (Math.sin(angle)) / f;
        buffer.pos(-scale1, -scale2, 0.0D).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(-scale1, scale2, 0.0D).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(scale1, scale2, 0.0D).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(scale1, -scale2, 0.0D).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();
    }
}
