package mcjty.meecreeps.entities;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * meecreep - wiiv
 * Created using Tabula 7.0.0
 */
public class MeeCreepsModel extends ModelBiped {
    public ModelRenderer hair_1;
    public ModelRenderer hair_2;
    public ModelRenderer hair_3;
    public ModelRenderer hair_4;

    public MeeCreepsModel() {

        this.textureWidth = 64;
        this.textureHeight = 64;

        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.setRotationPoint(0.0F, -6.0F, -0.0F);
        this.bipedHead.addBox(-5.0F, -10.0F, -5.0F, 10, 10, 10, 0.0F);
        this.hair_1 = new ModelRenderer(this, 32, 0);
        this.hair_1.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_1.addBox(-1.0F, -3.0F, 0.0F, 1, 3, 0, 0.0F);
        this.setRotateAngle(hair_1, 0.0F, 0.0F, -0.2617993877991494F);
        this.hair_2 = new ModelRenderer(this, 32, 0);
        this.hair_2.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_2.addBox(0.0F, -3.0F, 0.0F, 1, 3, 0, 0.0F);
        this.setRotateAngle(hair_2, 0.0F, 0.0F, 0.2617993877991494F);
        this.hair_3 = new ModelRenderer(this, 32, -1);
        this.hair_3.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_3.addBox(0.0F, -3.0F, -1.0F, 0, 3, 1, 0.0F);
        this.setRotateAngle(hair_3, 0.2617993877991494F, 0.0F, 0.0F);
        this.hair_4 = new ModelRenderer(this, 32, -1);
        this.hair_4.setRotationPoint(0.0F, -10.0F, 0.0F);
        this.hair_4.addBox(0.0F, -3.0F, 0.0F, 0, 3, 1, 0.0F);
        this.setRotateAngle(hair_4, -0.2617993877991494F, 0.0F, 0.0F);

        this.bipedBody = new ModelRenderer(this, 0, 20);
        this.bipedBody.setRotationPoint(0.0F, -6.0F, -0.0F);
        this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 14, 4, 0.0F);

        this.bipedRightArm = new ModelRenderer(this, 56, 0);
        this.bipedRightArm.setRotationPoint(-5.0F, -4.0F, 0.0F);
        this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 20, 2, 0.0F);
        this.setRotateAngle(bipedRightArm, 0.0F, 0.0F, 0.08726646259971647F);
        this.bipedLeftArm = new ModelRenderer(this, 56, 0);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.setRotationPoint(5.0F, -4.0F, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 20, 2, 0.0F);
        this.setRotateAngle(bipedLeftArm, 0.0F, 0.0F, -0.08726646259971647F);

        this.bipedRightLeg = new ModelRenderer(this, 56, 0);
        this.bipedRightLeg.setRotationPoint(-2.0F, 1.0F, 0.0F);
        this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 24, 2, 0.0F);
        this.bipedLeftLeg = new ModelRenderer(this, 56, 0);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.setRotationPoint(2.0F, 1.0F, 0.0F);
        this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 24, 2, 0.0F);

        this.bipedHead.addChild(this.hair_1);
        this.bipedHead.addChild(this.hair_4);
        this.bipedHead.addChild(this.hair_3);
        this.bipedHead.addChild(this.hair_2);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.bipedLeftArm.render(f5);
        this.bipedRightLeg.render(f5);
        this.bipedRightArm.render(f5);
        this.bipedHead.render(f5);
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
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netbipedHeadYaw, float bipedHeadPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netbipedHeadYaw, bipedHeadPitch, scaleFactor, entityIn);

        this.bipedHead.showModel = true;
        float f = -14.0F;
        this.bipedBody.rotateAngleX = 0.0F;
        this.bipedBody.rotationPointY = -6.0F;
        this.bipedBody.rotationPointZ = -0.0F;
        this.bipedRightLeg.rotateAngleX -= 0.0F;
        this.bipedLeftLeg.rotateAngleX -= 0.0F;
        this.bipedRightArm.rotateAngleX = (float)((double)this.bipedRightArm.rotateAngleX * 0.5D);
        this.bipedLeftArm.rotateAngleX = (float)((double)this.bipedLeftArm.rotateAngleX * 0.5D);
        this.bipedRightLeg.rotateAngleX = (float)((double)this.bipedRightLeg.rotateAngleX * 0.5D);
        this.bipedLeftLeg.rotateAngleX = (float)((double)this.bipedLeftLeg.rotateAngleX * 0.5D);
        float f1 = 0.4F;

        if (this.bipedRightArm.rotateAngleX > 0.4F)
        {
            this.bipedRightArm.rotateAngleX = 0.4F;
        }

        if (this.bipedLeftArm.rotateAngleX > 0.4F)
        {
            this.bipedLeftArm.rotateAngleX = 0.4F;
        }

        if (this.bipedRightArm.rotateAngleX < -0.4F)
        {
            this.bipedRightArm.rotateAngleX = -0.4F;
        }

        if (this.bipedLeftArm.rotateAngleX < -0.4F)
        {
            this.bipedLeftArm.rotateAngleX = -0.4F;
        }

        if (this.bipedRightLeg.rotateAngleX > 0.4F)
        {
            this.bipedRightLeg.rotateAngleX = 0.4F;
        }

        if (this.bipedLeftLeg.rotateAngleX > 0.4F)
        {
            this.bipedLeftLeg.rotateAngleX = 0.4F;
        }

        if (this.bipedRightLeg.rotateAngleX < -0.4F)
        {
            this.bipedRightLeg.rotateAngleX = -0.4F;
        }

        if (this.bipedLeftLeg.rotateAngleX < -0.4F)
        {
            this.bipedLeftLeg.rotateAngleX = -0.4F;
        }

//        if (this.isCarrying)
//        {
//            this.bipedRightArm.rotateAngleX = -0.5F;
//            this.bipedLeftArm.rotateAngleX = -0.5F;
//            this.bipedRightArm.rotateAngleZ = 0.05F;
//            this.bipedLeftArm.rotateAngleZ = -0.05F;
//        }

        this.bipedRightArm.rotationPointZ = 0.0F;
        this.bipedLeftArm.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotationPointZ = 0.0F;
        this.bipedLeftLeg.rotationPointZ = 0.0F;
        this.bipedRightLeg.rotationPointY = 1.0F;
        this.bipedLeftLeg.rotationPointY = 1.0F;
        this.bipedHead.rotationPointZ = -0.0F;
        this.bipedHead.rotationPointY = 6.0F;
        this.bipedHeadwear.rotationPointX = this.bipedHead.rotationPointX;
        this.bipedHeadwear.rotationPointY = this.bipedHead.rotationPointY;
        this.bipedHeadwear.rotationPointZ = this.bipedHead.rotationPointZ;
        this.bipedHeadwear.rotateAngleX = this.bipedHead.rotateAngleX;
        this.bipedHeadwear.rotateAngleY = this.bipedHead.rotateAngleY;
        this.bipedHeadwear.rotateAngleZ = this.bipedHead.rotateAngleZ;
    }
}

