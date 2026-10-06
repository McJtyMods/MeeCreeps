package mcjty.meecreeps.entities;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Original Tabula geometry by wiiv, converted to baked model parts. */
public class MeeCreepsModel extends EntityModel<EntityMeeCreeps> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("meecreeps", "meecreeps"), "main");
    private final ModelPart root;

    public MeeCreepsModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < 9; i++) {
            root.addOrReplaceChild("face" + i, CubeListBuilder.create().texOffs(0, 18 + 20 * i).addBox(-5, -10, -5, 10, 10, 10), PartPose.offset(0, -6, 0));
            root.addOrReplaceChild("hair" + i, CubeListBuilder.create().texOffs(36, 0).addBox(-1, .5F, -1, 2, 2, 2), PartPose.offset(0, -8, 0));
        }
        root.addOrReplaceChild("bipedBody", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, 0.0F, -2.0F, 6, 14, 4), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0, 0, 0));
        root.addOrReplaceChild("bipedRightArm", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 18, 2), PartPose.offsetAndRotation(-3.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));
        root.addOrReplaceChild("bipedLeftArm", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 2, 18, 2), PartPose.offsetAndRotation(3.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.08726646259971647F));
        root.addOrReplaceChild("bipedRightLeg", CubeListBuilder.create().texOffs(56, 0).addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2), PartPose.offsetAndRotation(-2.0F, 14.0F, 0.0F, 0, 0, 0));
        root.addOrReplaceChild("bipedLeftLeg", CubeListBuilder.create().texOffs(56, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2, 16, 2), PartPose.offsetAndRotation(2.0F, 14.0F, 0.0F, 0, 0, 0));
        root.getChild("hair0").addOrReplaceChild("hair_01", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.5235987755982988F, 0.0F, 1.3089969389957472F));
        root.getChild("hair0").addOrReplaceChild("hair_02", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, 1.0471975511965976F));
        root.getChild("hair0").addOrReplaceChild("hair_03", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.5235987755982988F, 0.0F, 1.3089969389957472F));
        root.getChild("hair0").addOrReplaceChild("hair_04", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.5235987755982988F, 0.0F, -1.3089969389957472F));
        root.getChild("hair0").addOrReplaceChild("hair_05", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -1.0471975511965976F));
        root.getChild("hair0").addOrReplaceChild("hair_06", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.5235987755982988F, 0.0F, -1.3089969389957472F));
        root.getChild("hair1").addOrReplaceChild("hair_11", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 0.0F, -0.5235987755982988F));
        root.getChild("hair1").addOrReplaceChild("hair_12", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.0F, 3.141592653589793F, 0.5235987755982988F));
        root.getChild("hair1").addOrReplaceChild("hair_13", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0, 0, 0));
        root.getChild("hair1").addOrReplaceChild("hair_14", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.5235987755982988F, 0.0F, 0.0F));
        root.getChild("hair2").addOrReplaceChild("hair_21", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.7853981633974483F, 0.0F, -0.5235987755982988F));
        root.getChild("hair2").addOrReplaceChild("hair_22", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.7853981633974483F, 3.141592653589793F, 0.5235987755982988F));
        root.getChild("hair2").addOrReplaceChild("hair_23", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.0471975511965976F, 0.0F, 0.0F));
        root.getChild("hair2").addOrReplaceChild("hair_24", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -1.0471975511965976F, 0.0F, 0.0F));
        root.getChild("hair3").addOrReplaceChild("hair_31", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.3089969389957472F, 0.0F, 0.0F));
        root.getChild("hair3").addOrReplaceChild("hair_32", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.2617993877991494F));
        root.getChild("hair3").addOrReplaceChild("hair_33", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.7853981633974483F, 0.0F, 0.0F));
        root.getChild("hair3").addOrReplaceChild("hair_34", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 0.7853981633974483F, 0.0F, 0.0F));
        root.getChild("hair3").addOrReplaceChild("hair_35", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.2617993877991494F, 0.0F, -0.2617993877991494F));
        root.getChild("hair3").addOrReplaceChild("hair_36", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -1.3089969389957472F, 0.0F, 0.0F));
        root.getChild("hair4").addOrReplaceChild("hair_41", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.3089969389957472F, -0.7853981633974483F, 0.0F));
        root.getChild("hair4").addOrReplaceChild("hair_42", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.0471975511965976F, 0.5235987755982988F, 0.0F));
        root.getChild("hair4").addOrReplaceChild("hair_43", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.3089969389957472F, 0.7853981633974483F, 0.0F));
        root.getChild("hair4").addOrReplaceChild("hair_44", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.0471975511965976F, -0.5235987755982988F, 0.0F));
        root.getChild("hair5").addOrReplaceChild("hair_51", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.0471975511965976F, 0.5235987755982988F, 0.0F));
        root.getChild("hair5").addOrReplaceChild("hair_52", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.0471975511965976F, -0.5235987755982988F, 0.0F));
        root.getChild("hair5").addOrReplaceChild("hair_53", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, 1.3089969389957472F, 0.0F, 0.0F));
        root.getChild("hair5").addOrReplaceChild("hair_54", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.2617993877991494F, 0.0F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_61", CubeListBuilder.create().texOffs(40, 18).addBox(-1.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -1.0471975511965976F, -0.5235987755982988F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_62", CubeListBuilder.create().texOffs(40, 18).addBox(0.0F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -1.0471975511965976F, 0.5235987755982988F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_63", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -10.0F, 0.0F, -0.2617993877991494F, 0.0F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_64", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(5.0F, -5.0F, 0.0F, 2.356194490192345F, 0.5235987755982988F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_65", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(5.0F, -5.0F, 0.0F, 1.8325957145940461F, 0.2617993877991494F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_66", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(5.0F, -5.0F, 0.0F, 1.3089969389957472F, 0.2617993877991494F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_67", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, 0.0F, 1, 3, 1), PartPose.offsetAndRotation(-5.0F, -5.0F, 0.0F, 2.356194490192345F, -0.5235987755982988F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_68", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(-5.0F, -5.0F, 0.0F, 1.8325957145940461F, -0.2617993877991494F, 0.0F));
        root.getChild("hair6").addOrReplaceChild("hair_69", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, 0.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(-5.0F, -5.0F, 0.0F, 1.3089969389957472F, -0.2617993877991494F, 0.0F));
        root.getChild("hair7").addOrReplaceChild("hair_71", CubeListBuilder.create().texOffs(40, 18).addBox(-0.5F, -3.0F, -1.0F, 1, 3, 1), PartPose.offsetAndRotation(0.0F, -9.0F, -5.0F, -0.2617993877991494F, 0.0F, 0.2617993877991494F));
        return LayerDefinition.create(mesh, 64, 256);
    }

    @Override
    public void setupAnim(EntityMeeCreeps e, float swing, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        for (int i = 0; i < 9; i++) {
            ModelPart face = root.getChild("face" + i), hair = root.getChild("hair" + i);
            face.visible = i == e.getVariationFace();
            hair.visible = i == e.getVariationHair();
            face.xRot = hair.xRot = pitch * Mth.DEG_TO_RAD;
            face.yRot = hair.yRot = yaw * Mth.DEG_TO_RAD;
        }
        ModelPart right = root.getChild("bipedRightArm"), left = root.getChild("bipedLeftArm");
        right.xRot = Mth.clamp(Mth.cos(swing * .6662F + (float) Math.PI) * amount, -.4F, .4F);
        left.xRot = Mth.clamp(Mth.cos(swing * .6662F) * amount, -.4F, .4F);
        root.getChild("bipedRightLeg").xRot = left.xRot;
        root.getChild("bipedLeftLeg").xRot = right.xRot;
        root.getChild("bipedRightLeg").y = root.getChild("bipedLeftLeg").y = 8;
        if (e.getHeldBlockState() != null) {
            right.xRot = left.xRot = -.5F;
            right.zRot = .05F;
            left.zRot = -.05F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(pose, vertices, light, overlay, color);
    }
}
