package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AngryActionWorker extends AbstractActionWorker {

    @Override
    public AxisAlignedBB getActionBox() {
        return null;
    }

    @Override
    public void init(IMeeCreep meeCreep) {
        ((EntityMeeCreeps) meeCreep).setVariationFace(1);
    }

    public AngryActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
        } else if (findMeeCreeps()) {
        }
    }

    private void attack(EntityMeeCreeps enemy) {
        IMeeCreep entity = helper.getMeeCreep();
        enemy.attackEntityFrom(DamageSource.causeMobDamage(entity.getEntity()), 4.0F);
    }

    private boolean findMeeCreeps() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos position = entity.getEntity().getPosition();
        List<EntityMeeCreeps> meeCreeps = entity.getWorld().getEntitiesWithinAABB(EntityMeeCreeps.class, getSearchBox(),
                input -> input != entity.getEntity());
        if (!meeCreeps.isEmpty()) {
            meeCreeps.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityMeeCreeps enemy = meeCreeps.get(0);
            helper.navigateTo(enemy, (pos) -> attack(enemy));
            return true;
        }
        return false;
    }

}