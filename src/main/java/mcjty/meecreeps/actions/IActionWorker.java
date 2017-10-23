package mcjty.meecreeps.actions;

import mcjty.meecreeps.entities.EntityMeeCreeps;

public interface IActionWorker {

    void tick(EntityMeeCreeps entity, boolean lastTask);
}
