package mcjty.meecreeps.entities;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * meecreep - wiiv
 * Created using Tabula 7.0.0
 */
public class MeeCreepsModel extends ModelBiped {

    public boolean isCarrying;

    public ModelRenderer hair_01;
    public ModelRenderer hair_02;
    public ModelRenderer hair_03;
    public ModelRenderer hair_04;
    public ModelRenderer hair_05;
    public ModelRenderer hair_06;

    public ModelRenderer hair_11;
    public ModelRenderer hair_12;
    public ModelRenderer hair_13;
    public ModelRenderer hair_14;

    public ModelRenderer hair_21;
    public ModelRenderer hair_22;
    public ModelRenderer hair_23;
    public ModelRenderer hair_24;

    public ModelRenderer hair_31;
    public ModelRenderer hair_32;
    public ModelRenderer hair_33;
    public ModelRenderer hair_34;
    public ModelRenderer hair_35;
    public ModelRenderer hair_36;

    public ModelRenderer hair_41;
    public ModelRenderer hair_42;
    public ModelRenderer hair_43;
    public ModelRenderer hair_44;

    public ModelRenderer hair_51;
    public ModelRenderer hair_52;
    public ModelRenderer hair_53;
    public ModelRenderer hair_54;

    public ModelRenderer hair_61;
    public ModelRenderer hair_62;
    public ModelRenderer hair_63;
    public ModelRenderer hair_64;
    public ModelRenderer hair_65;
    public ModelRenderer hair_66;
    public ModelRenderer hair_67;
    public ModelRenderer hair_68;
    public ModelRenderer hair_69;

    public ModelRenderer hair_71;

    private ModelRenderer faceVariation[] = new ModelRenderer[9];
    private ModelRenderer hairVariation[] = new ModelRenderer[9];

    public MeeCreepsModel() {

        this.textureWidth = 64;
        this.textureHeight = 256;

        this.bipedHead = new ModelRenderer(this, 0, 18);
        this.bipedHead.setRotationPoint(0.0F, -6.0F, -0.0F);
        this.bipedHead.addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10, 0.0F);

        for (int i = 0; i < 9; i++) {
            this.faceVariation[i] = new ModelRenderer(this, 0, getTextureV(i));
            this.faceVariation[i].setRotationPoint(0.0F, -6.0F, 0.0F);
            this.faceVariation[i].addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10, 0.0F);
        }

        for (int i = 0; i < 9; i++) {
            this.hairVariation[i] = new ModelRenderer(this, 36, 0);
            this.hairVariation[i].setRotationPoint(0.0F, -8.0F, 0.0F);
            this.hairVariation[i].addBox(-1.0F, 0.5F, -1.0F, 2, 2, 2, 0.0F);
        }

        this.bipedBody = new ModelRenderer(this, 0, 0);
        this.bipedBody.setRotationPoint(0.0F, -6.0F, 0.0F);
        this.bipedBody.addBox(-3.0F, 0.0F, -2.0F, 6, 14, 4, 0.0F);

        this.bipedRightArm = new ModelRenderer(this, 48, 0);
        this.bipedRightArm.setRotationPoint(-3.0F, -5.0F, 0.0F);
        this.bipedRightArm.addBox(-1.0F, -1.0F, -1.0F, 2, 18, 2, 0.0F);
        this.setRotateAngle(bipedRightArm, 0.0F, 0.0F, 0.08726646259971647F);
        this.bipedLeftArm = new ModelRenderer(this, 48, 0);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.setRotationPoint(3.0F, -5.0F, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -1.0F, -1.0F, 2, 18, 2, 0.0F);
        this.setRotateAngle(bipedLeftArm, 0.0F, 0.0F, -0.08726646259971647F);

        this.bipedRightLeg = new ModelRenderer(this, 56, 0);
        this.bipedRightLeg.setRotationPoint(-2.0F, 14.0F, 0.0F);
        this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2, 0.0F);
        this.bipedLeftLeg = new ModelRenderer(this, 56, 0);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.setRotationPoint(2.0F, 14.0F, 0.0F);
        this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2, 0.0F);

        this.hair_01 = new ModelRenderer(this, 40, 18);
        this.hair_01.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_01.addBox(0.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_01, 0.5235987755982988F, 0.0F, 1.3089969389957472F);
        this.hair_02 = new ModelRenderer(this, 40, 18);
        this.hair_02.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_02.addBox(0.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_02, 0.0F, 0.0F, 1.0471975511965976F);
        this.hair_03 = new ModelRenderer(this, 40, 18);
        this.hair_03.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_03.addBox(0.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_03, -0.5235987755982988F, 0.0F, 1.3089969389957472F);
        this.hair_04 = new ModelRenderer(this, 40, 18);
        this.hair_04.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_04.addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_04, 0.5235987755982988F, 0.0F, -1.3089969389957472F);
        this.hair_05 = new ModelRenderer(this, 40, 18);
        this.hair_05.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_05.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_05, 0.0F, 0.0F, -1.0471975511965976F);
        this.hair_06 = new ModelRenderer(this, 40, 18);
        this.hair_06.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_06.addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_06, -0.5235987755982988F, 0.0F, -1.3089969389957472F);

        this.hair_11 = new ModelRenderer(this, 40, 18);
        this.hair_11.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_11.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_11, 0.0F, 0.0F, -0.5235987755982988F);
        this.hair_12 = new ModelRenderer(this, 40, 18);
        this.hair_12.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_12.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_12, 0.0F, 3.141592653589793F, 0.5235987755982988F);
        this.hair_13 = new ModelRenderer(this, 40, 18);
        this.hair_13.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_13.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.hair_14 = new ModelRenderer(this, 40, 18);
        this.hair_14.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_14.addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_14, -0.5235987755982988F, 0.0F, 0.0F);

        this.hair_21 = new ModelRenderer(this, 40, 18);
        this.hair_21.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_21.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_21, 0.7853981633974483F, 0.0F, -0.5235987755982988F);
        this.hair_22 = new ModelRenderer(this, 40, 18);
        this.hair_22.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_22.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_22, 0.7853981633974483F, 3.141592653589793F, 0.5235987755982988F);
        this.hair_23 = new ModelRenderer(this, 40, 18);
        this.hair_23.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_23.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_23, 1.0471975511965976F, 0.0F, 0.0F);
        this.hair_24 = new ModelRenderer(this, 40, 18);
        this.hair_24.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_24.addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_24, -1.0471975511965976F, 0.0F, 0.0F);

        this.hair_31 = new ModelRenderer(this, 40, 18);
        this.hair_31.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_31.addBox(0.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_31, 1.3089969389957472F, 0.0F, 0.0F);
        this.hair_32 = new ModelRenderer(this, 40, 18);
        this.hair_32.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_32.addBox(0.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_32, 0.2617993877991494F, 0.0F, 0.2617993877991494F);
        this.hair_33 = new ModelRenderer(this, 40, 18);
        this.hair_33.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_33.addBox(0.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_33, -0.7853981633974483F, 0.0F, 0.0F);
        this.hair_34 = new ModelRenderer(this, 40, 18);
        this.hair_34.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_34.addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_34, 0.7853981633974483F, 0.0F, 0.0F);
        this.hair_35 = new ModelRenderer(this, 40, 18);
        this.hair_35.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_35.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_35, -0.2617993877991494F, 0.0F, -0.2617993877991494F);
        this.hair_36 = new ModelRenderer(this, 40, 18);
        this.hair_36.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_36.addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_36, -1.3089969389957472F, 0.0F, 0.0F);

        this.hair_41 = new ModelRenderer(this, 40, 18);
        this.hair_41.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_41.addBox(0.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_41, 1.3089969389957472F, -0.7853981633974483F, 0.0F);
        this.hair_42 = new ModelRenderer(this, 40, 18);
        this.hair_42.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_42.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_42, 1.0471975511965976F, 0.5235987755982988F, 0.0F);
        this.hair_43 = new ModelRenderer(this, 40, 18);
        this.hair_43.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_43.addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_43, 1.3089969389957472F, 0.7853981633974483F, 0.0F);
        this.hair_44 = new ModelRenderer(this, 40, 18);
        this.hair_44.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_44.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_44, 1.0471975511965976F, -0.5235987755982988F, 0.0F);

        this.hair_51 = new ModelRenderer(this, 40, 18);
        this.hair_51.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_51.addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_51, 1.0471975511965976F, 0.5235987755982988F, 0.0F);
        this.hair_52 = new ModelRenderer(this, 40, 18);
        this.hair_52.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_52.addBox(0.0F, -3.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_52, 1.0471975511965976F, -0.5235987755982988F, 0.0F);
        this.hair_53 = new ModelRenderer(this, 40, 18);
        this.hair_53.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_53.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_53, 1.3089969389957472F, 0.0F, 0.0F);
        this.hair_54 = new ModelRenderer(this, 40, 18);
        this.hair_54.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_54.addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_54, -0.2617993877991494F, 0.0F, 0.0F);

        this.hair_61 = new ModelRenderer(this, 40, 18);
        this.hair_61.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_61.addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_61, -1.0471975511965976F, -0.5235987755982988F, 0.0F);
        this.hair_62 = new ModelRenderer(this, 40, 18);
        this.hair_62.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_62.addBox(0.0F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_62, -1.0471975511965976F, 0.5235987755982988F, 0.0F);
        this.hair_63 = new ModelRenderer(this, 40, 18);
        this.hair_63.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_63.addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_63, -0.2617993877991494F, 0.0F, 0.0F);
        this.hair_64 = new ModelRenderer(this, 40, 18);
        this.hair_64.setRotationPoint(5.0F, -5.0F, 0.0F);
        this.hair_64.addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_64, 2.356194490192345F, 0.5235987755982988F, 0.0F);
        this.hair_65 = new ModelRenderer(this, 40, 18);
        this.hair_65.setRotationPoint(5.0F, -5.0F, 0.0F);
        this.hair_65.addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_65, 1.8325957145940461F, 0.2617993877991494F, 0.0F);
        this.hair_66 = new ModelRenderer(this, 40, 18);
        this.hair_66.setRotationPoint(5.0F, -5.0F, 0.0F);
        this.hair_66.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_66, 1.3089969389957472F, 0.2617993877991494F, 0.0F);
        this.hair_67 = new ModelRenderer(this, 40, 18);
        this.hair_67.setRotationPoint(-5.0F, -5.0F, 0.0F);
        this.hair_67.addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_67, 2.356194490192345F, -0.5235987755982988F, 0.0F);
        this.hair_68 = new ModelRenderer(this, 40, 18);
        this.hair_68.setRotationPoint(-5.0F, -5.0F, 0.0F);
        this.hair_68.addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_68, 1.8325957145940461F, -0.2617993877991494F, 0.0F);
        this.hair_69 = new ModelRenderer(this, 40, 18);
        this.hair_69.setRotationPoint(-5.0F, -5.0F, 0.0F);
        this.hair_69.addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_69, 1.3089969389957472F, -0.2617993877991494F, 0.0F);

        this.hair_71 = new ModelRenderer(this, 40, 18);
        this.hair_71.setRotationPoint(0.0F, -9.0F, -5.0F);
        this.hair_71.addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1, 0.0F);
        this.setRotateAngle(hair_71, -0.2617993877991494F, 0.0F, 0.2617993877991494F);

        for (int i = 0; i < 9; i++) {
            this.bipedHead.addChild(this.hairVariation[i]);
        }

        this.hairVariation[0].addChild(this.hair_01);
        this.hairVariation[0].addChild(this.hair_02);
        this.hairVariation[0].addChild(this.hair_03);
        this.hairVariation[0].addChild(this.hair_04);
        this.hairVariation[0].addChild(this.hair_05);
        this.hairVariation[0].addChild(this.hair_06);

        this.hairVariation[1].addChild(this.hair_11);
        this.hairVariation[1].addChild(this.hair_12);
        this.hairVariation[1].addChild(this.hair_13);
        this.hairVariation[1].addChild(this.hair_14);

        this.hairVariation[2].addChild(this.hair_21);
        this.hairVariation[2].addChild(this.hair_22);
        this.hairVariation[2].addChild(this.hair_23);
        this.hairVariation[2].addChild(this.hair_24);

        this.hairVariation[3].addChild(this.hair_31);
        this.hairVariation[3].addChild(this.hair_32);
        this.hairVariation[3].addChild(this.hair_33);
        this.hairVariation[3].addChild(this.hair_34);
        this.hairVariation[3].addChild(this.hair_35);
        this.hairVariation[3].addChild(this.hair_36);

        this.hairVariation[4].addChild(this.hair_41);
        this.hairVariation[4].addChild(this.hair_42);
        this.hairVariation[4].addChild(this.hair_43);
        this.hairVariation[4].addChild(this.hair_44);

        this.hairVariation[5].addChild(this.hair_51);
        this.hairVariation[5].addChild(this.hair_52);
        this.hairVariation[5].addChild(this.hair_53);
        this.hairVariation[5].addChild(this.hair_54);

        this.hairVariation[6].addChild(this.hair_61);
        this.hairVariation[6].addChild(this.hair_62);
        this.hairVariation[6].addChild(this.hair_63);
        this.hairVariation[6].addChild(this.hair_64);
        this.hairVariation[6].addChild(this.hair_65);
        this.hairVariation[6].addChild(this.hair_66);
        this.hairVariation[6].addChild(this.hair_67);
        this.hairVariation[6].addChild(this.hair_68);
        this.hairVariation[6].addChild(this.hair_69);

        this.hairVariation[7].addChild(this.hair_71);
    }

    private int getTextureV(int variation) {
        int v = 18;
        switch (variation) {
            case 0:
                break;
            case 1:
                v = 38;
                break;
            case 2:
                v = 58;
                break;
            case 3:
                v = 78;
                break;
            case 4:
                v = 98;
                break;
            case 5:
                v = 118;
                break;
            case 6:
                v = 138;
                break;
            case 7:
                v = 158;
                break;
            case 8:
                v = 178;
                break;
        }
        return v;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        EntityMeeCreeps meeCreeps = (EntityMeeCreeps) entity;
        this.bipedLeftArm.render(f5);
        this.bipedRightLeg.render(f5);
        this.bipedRightArm.render(f5);
        this.faceVariation[meeCreeps.getVariationFace()].render(f5);
        this.hairVariation[meeCreeps.getVariationHair()].render(f5);
        this.bipedLeftLeg.render(f5);
        this.bipedBody.render(f5);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netbipedHeadYaw, float bipedHeadPitch, float scaleFactor, Entity entity) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netbipedHeadYaw, bipedHeadPitch, scaleFactor, entity);

        this.bipedHead.showModel = true;
        float f = -14.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotationPointY = -6.0F;
        this.bipedBody.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotateAngleX -= 0.0F;
        this.bipedLeftLeg.rotateAngleX -= 0.0F;
        this.bipedRightArm.rotateAngleX = (float) ((double) this.bipedRightArm.rotateAngleX * 0.5D);
        this.bipedLeftArm.rotateAngleX = (float) ((double) this.bipedLeftArm.rotateAngleX * 0.5D);
        this.bipedRightLeg.rotateAngleX = (float) ((double) this.bipedRightLeg.rotateAngleX * 0.5D);
        this.bipedLeftLeg.rotateAngleX = (float) ((double) this.bipedLeftLeg.rotateAngleX * 0.5D);

        for (int i = 0; i < 9; i++) {
            this.hairVariation[i].rotationPointY = -8.0F;
        }

        float f1 = 0.4F;

        if (this.bipedRightArm.rotateAngleX > 0.4F) {
            this.bipedRightArm.rotateAngleX = 0.4F;
        }

        if (this.bipedLeftArm.rotateAngleX > 0.4F) {
            this.bipedLeftArm.rotateAngleX = 0.4F;
        }

        if (this.bipedRightArm.rotateAngleX < -0.4F) {
            this.bipedRightArm.rotateAngleX = -0.4F;
        }

        if (this.bipedLeftArm.rotateAngleX < -0.4F) {
            this.bipedLeftArm.rotateAngleX = -0.4F;
        }

        if (this.bipedRightLeg.rotateAngleX > 0.4F) {
            this.bipedRightLeg.rotateAngleX = 0.4F;
        }

        if (this.bipedLeftLeg.rotateAngleX > 0.4F) {
            this.bipedLeftLeg.rotateAngleX = 0.4F;
        }

        if (this.bipedRightLeg.rotateAngleX < -0.4F) {
            this.bipedRightLeg.rotateAngleX = -0.4F;
        }

        if (this.bipedLeftLeg.rotateAngleX < -0.4F) {
            this.bipedLeftLeg.rotateAngleX = -0.4F;
        }

        if (this.isCarrying) {
            this.bipedRightArm.rotateAngleX = -0.5F;
            this.bipedLeftArm.rotateAngleX = -0.5F;
            this.bipedRightArm.rotateAngleZ = 0.05F;
            this.bipedLeftArm.rotateAngleZ = -0.05F;
        }

        this.bipedRightArm.rotationPointZ = 0.0F;
        this.bipedRightArm.rotationPointX = -4.0F;
        this.bipedLeftArm.rotationPointZ = 0.0F;
        this.bipedLeftArm.rotationPointX = 4.0F;
        this.bipedRightLeg.rotationPointZ = 0.0F;
        this.bipedLeftLeg.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotationPointY = 8.0F;
        this.bipedLeftLeg.rotationPointY = 8.0F;
        this.bipedHead.rotationPointZ = 0.0F;
        this.bipedHead.rotationPointY = -6.0F;
        this.bipedHeadwear.rotationPointX = this.bipedHead.rotationPointX;
        this.bipedHeadwear.rotationPointY = this.bipedHead.rotationPointY;
        this.bipedHeadwear.rotationPointZ = this.bipedHead.rotationPointZ;
        this.bipedHeadwear.rotateAngleX = this.bipedHead.rotateAngleX;
        this.bipedHeadwear.rotateAngleY = this.bipedHead.rotateAngleY;
        this.bipedHeadwear.rotateAngleZ = this.bipedHead.rotateAngleZ;

        EntityMeeCreeps meeCreeps = (EntityMeeCreeps) entity;
        copyModelAngles(bipedHead, faceVariation[meeCreeps.getVariationFace()]);
        copyModelAngles(bipedHead, hairVariation[meeCreeps.getVariationHair()]);
    }
}

