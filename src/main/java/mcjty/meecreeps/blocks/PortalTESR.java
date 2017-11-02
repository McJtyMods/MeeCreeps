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

//        Minecraft mc = Minecraft.getMinecraft();
//        EntityPlayerSP p = mc.player;
//        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTicks;
//        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTicks;
//        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTicks;
//        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);

        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
//        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate((float) x + 0.5F, (float) y + 1F, (float) z + 0.5F);
        this.bindTexture(portal);
        renderQuadBright(1.0f, angle);
        angle += .03;

//        GlStateManager.enableBlend();
//        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

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
