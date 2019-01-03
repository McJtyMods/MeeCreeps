package mcjty.meecreeps.blocks;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.opengl.GL11;

public class PortalTESR extends TileEntitySpecialRenderer<PortalTileEntity> {

    private static final ResourceLocation portal = new ResourceLocation(MeeCreeps.MODID, "textures/effects/portal.png");

    private static double angle = 0;

    @Override
    public void render(PortalTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        if (te.getPortalSide() == null) {
            return;
        }

        GlStateManager.pushMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

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

        Face face = faces[te.getPortalSide().ordinal()];
        Face rface = revertedfaces[te.getPortalSide().ordinal()];

        GlStateManager.translate((float) x + face.ox, (float) y + face.oy, (float) z + face.oz);
        GlStateManager.scale(scale, scale, scale);
        renderQuadBright(angle, face, rface);

        GlStateManager.popMatrix();
    }


    public static void renderQuadBright(double angle, Face face, Face rface) {
        int brightness = 240;
        int b1 = brightness >> 16 & 65535;
        int b2 = brightness & 65535;
        GlStateManager.pushMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

        double f = 1.75;
        renderFace(face, angle, b1, b2, buffer, f, 200);
        renderFace(face, -angle, b1, b2, buffer, f, 60);
        renderFace(rface, angle, b1, b2, buffer, f, 200);
        renderFace(rface, -angle, b1, b2, buffer, f, 60);


        tessellator.draw();
        GlStateManager.popMatrix();
    }

    private static Face[] faces = new Face[6];
    private static Face[] revertedfaces = new Face[6];

    static {
        double half = 1.0;
        faces[EnumFacing.DOWN.ordinal()] =  new Face(0.5, 0.9, 0.5,    -1, 0, -1,       -1, 0, 1,      1, 0, 1,      1, 0, -1);
        faces[EnumFacing.UP.ordinal()] =    new Face(0.5, 0.1, 0.5,    -1, 0, -1,       -1, 0, 1,      1, 0, 1,      1, 0, -1);
        faces[EnumFacing.SOUTH.ordinal()] = new Face(0.5, 1, 0.1,      -half, -1, 0,    -half, 1, 0,   half, 1, 0,   half, -1, 0);
        faces[EnumFacing.NORTH.ordinal()] = new Face(0.5, 1, 0.9,      -half, -1, 0,    -half, 1, 0,   half, 1, 0,   half, -1, 0);
        faces[EnumFacing.EAST.ordinal()] =  new Face(0.1, 1, 0.5,       0, -1, -half,   0, 1, -half,   0, 1, half,   0, -1, half);
        faces[EnumFacing.WEST.ordinal()] =  new Face(0.9, 1, 0.5,       0, -1, -half,   0, 1, -half,   0, 1, half,   0, -1, half);
        for (EnumFacing facing : EnumFacing.VALUES) {
            revertedfaces[facing.ordinal()] = faces[facing.ordinal()].reverse();
        }
    }

    private static class Face {
        public final double ox;
        public final double oy;
        public final double oz;

        public final double x0;
        public final double y0;
        public final double z0;
        public final double x1;
        public final double y1;
        public final double z1;
        public final double x2;
        public final double y2;
        public final double z2;
        public final double x3;
        public final double y3;
        public final double z3;

        public Face(double ox, double oy, double oz, double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.x0 = x0;
            this.y0 = y0;
            this.z0 = z0;
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;
        }

        public Face reverse() {
            return new Face(ox, oy, oz, x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0);
        }
    }

    private static void renderFace(Face face, double angle, int b1, int b2, BufferBuilder buffer, double f, int alpha) {
        double u;
        double v;
        double swap;
        u = (Math.cos(angle)) / f;
        v = (Math.sin(angle)) / f;
        buffer.pos(face.x0, face.y0, face.z0).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(face.x1, face.y1, face.z1).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(face.x2, face.y2, face.z2).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();

        swap = u;
        u = -v;
        v = swap;
        buffer.pos(face.x3, face.y3, face.z3).tex(u+.5, v+.5).lightmap(b1, b2).color(255, 255, 255, alpha).endVertex();
    }

    public static void register() {
        ClientRegistry.bindTileEntitySpecialRenderer(PortalTileEntity.class, new PortalTESR());
    }
}
