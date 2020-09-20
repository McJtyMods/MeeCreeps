package mcjty.meecreeps.entities;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.meecreeps.setup.Registration.ENTITIES;

public class ModEntities {

    public static void register() {
        // todo: set this up with data gens
//        LootTableList.register(EntityMeeCreeps.LOOT);
    }

    public static final RegistryObject<EntityType<EntityMeeCreeps>> MEECREEPS_ENTITY = ENTITIES.register(
            "meecreeps",
            () -> EntityType.Builder.<EntityMeeCreeps>create(EntityMeeCreeps::new, EntityClassification.CREATURE)
                .size(0.6F, 1.95F)
                .setTrackingRange(64)
                .setUpdateInterval(3)
                .setShouldReceiveVelocityUpdates(true)
                .build("meecreeps")
    );

    public static final RegistryObject<EntityType<EntityProjectile>> PROJECTILE_ENTITY = ENTITIES.register(
            "meecreeps_projectile",
            () -> EntityType.Builder.<EntityProjectile>create(EntityProjectile::new, EntityClassification.MISC)
                    .size(0.6F, 1.95F)
                    .setTrackingRange(100)
                    .setUpdateInterval(5)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("meecreeps_projectile")
    );

    // I'm not sure on the side for this yet so I'm going to put it here for now
//    @SubscribeEvent
//    public static void registerOther() {
//        RenderingRegistry.registerEntityRenderingHandler(PROJECTILE_ENTITY.get(), RenderProjectile::new);
//    }
}
