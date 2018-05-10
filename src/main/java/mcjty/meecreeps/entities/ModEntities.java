package mcjty.meecreeps.entities;

import mcjty.meecreeps.MeeCreeps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModEntities {

    public static void init() {
        // Every entity in our mod has an ID (local to this mod)
        int id = 1;
        EntityRegistry.registerModEntity(new ResourceLocation(MeeCreeps.MODID, "meecreeps"), EntityMeeCreeps.class, "MeeCreeps", id++, MeeCreeps.instance, 64, 3, true, 0x0CD5F2, 0xFF7300);
        EntityRegistry.registerModEntity(new ResourceLocation(MeeCreeps.MODID, "meecreeps_projectile"), EntityProjectile.class, "meecreeps_projectile", id++, MeeCreeps.instance, 100, 5, true);

        // This is the loot table for our mob
        LootTableList.register(EntityMeeCreeps.LOOT);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        RenderingRegistry.registerEntityRenderingHandler(EntityMeeCreeps.class, RenderMeeCreeps.FACTORY);

        RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, new IRenderFactory<EntityProjectile>() {
            @Override
            public Render<? super EntityProjectile> createRenderFor(RenderManager manager) {
                return new RenderProjectile<EntityProjectile>(manager, Minecraft.getMinecraft().getRenderItem());
            }
        });
    }
}
