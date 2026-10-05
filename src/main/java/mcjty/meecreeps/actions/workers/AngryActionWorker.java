package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;

import java.util.List;

public class AngryActionWorker extends AbstractActionWorker {

    @Override
    public AABB getActionBox() {
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
        enemy.hurt(entity.getEntity().damageSources().mobAttack(entity.getEntity()), 4.0F);
    }

    private boolean findMeeCreeps() {
        IMeeCreep entity = helper.getMeeCreep();
        BlockPos position = entity.getEntity().blockPosition();
        List<EntityMeeCreeps> meeCreeps = entity.getWorld().getEntitiesOfClass(EntityMeeCreeps.class, getSearchBox(),
                input -> input != entity.getEntity());
        if (!meeCreeps.isEmpty()) {
            meeCreeps.sort((o1, o2) -> {
                double d1 = position.distToCenterSqr(o1.position());
                double d2 = position.distToCenterSqr(o2.position());
                return Double.compare(d1, d2);
            });
            EntityMeeCreeps enemy = meeCreeps.get(0);
            helper.navigateTo(enemy, (pos) -> attack(enemy));
            return true;
        }
        return false;
    }

}