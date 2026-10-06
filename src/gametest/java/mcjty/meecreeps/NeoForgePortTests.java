package mcjty.meecreeps;

import io.netty.buffer.Unpooled;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.actions.factories.HarvestActionFactory;
import mcjty.meecreeps.actions.workers.HarvestReplantActionWorker;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.items.*;
import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.teleport.TeleportDestination;
import mcjty.meecreeps.varia.EntityTeleportation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder("meecreeps")
@PrefixGameTestTemplate(false)
public class NeoForgePortTests {
    @GameTest(template = "empty")
    public static void componentsCopySyncAndRecipeRemainders(GameTestHelper test) {
        var registries = test.getLevel().registryAccess();
        ItemStack gun = new ItemStack(Registration.GUN.get());
        new ItemEnergy(gun).receiveEnergy(1250, false);
        PortalGunItem.addDestination(gun, new TeleportDestination("Home", Level.OVERWORLD,
                new BlockPos(3, 70, 4), Direction.NORTH), 2);
        ItemStack copy = gun.copy();
        PortalGunItem.setCharge(copy, 9);
        test.assertTrue(PortalGunItem.getCharge(gun) == 1, "Copy mutated original component data");
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            ItemStack.STREAM_CODEC.encode(buf, gun);
            ItemStack synced = ItemStack.STREAM_CODEC.decode(buf);
            test.assertTrue(ItemEnergy.stored(synced) == 1250, "Item sync lost partial energy");
            test.assertTrue(PortalGunItem.getCurrentDestination(synced) == 2
                    && PortalGunItem.getDestinations(synced).get(2).getName().equals("Home"), "Item sync lost destinations");
        } finally {
            buf.release();
        }
        var remove = new RemoveCartridgeFactory(CraftingBookCategory.MISC);
        var input = CraftingInput.of(1, 1, List.of(gun));
        ItemStack remainder = remove.getRemainingItems(input).getFirst();
        test.assertTrue(ItemEnergy.stored(remainder) == 0, "Empty gun retained removed cartridge energy");
        var insert = new InsertCartridgeFactory(CraftingBookCategory.MISC);
        ItemStack assembled = insert.assemble(CraftingInput.of(2, 1,
                List.of(remainder, new ItemStack(Registration.CARTRIDGE.get()))), registries);
        test.assertTrue(ItemEnergy.stored(assembled) == 0, "Empty cartridge inherited stale energy");
        test.assertTrue(PortalGunItem.getDestinations(assembled).get(2) != null, "Empty gun lost destinations");
        ItemStack cube = new ItemStack(Registration.CUBE_ITEM.get());
        CreepCubeItem.setLastAction(cube, new MeeCreepActionType("meecreeps.idle"), "old");
        CreepCubeItem.setLastAction(cube, new MeeCreepActionType("meecreeps.idle"), null);
        test.assertTrue(CreepCubeItem.getLastQuestionId(cube) == null, "Repeated action retained a stale question");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void harvestingAndReplantingUseBlockItemSeeds(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos chest = test.absolutePos(new BlockPos(1, 1, 1));
        BlockPos wheat = test.absolutePos(new BlockPos(3, 1, 3));
        BlockPos wart = test.absolutePos(new BlockPos(5, 1, 5));
        world.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
        world.setBlock(wheat.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        world.setBlock(wart.below(), Blocks.SOUL_SAND.defaultBlockState(), 3);
        var factory = new HarvestActionFactory();
        test.assertTrue(!factory.isPossibleSecondary(world, chest, Direction.UP), "Empty soil was treated as a crop");
        world.setBlock(wheat, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7), 3);
        world.setBlock(wart, Blocks.NETHER_WART.defaultBlockState().setValue(net.minecraft.world.level.block.NetherWartBlock.AGE, 3), 3);
        test.assertTrue(factory.isPossible(world, chest, Direction.UP), "Mature crops were not recognized");
        var options = new ActionOptions(List.of(), List.of(), chest, Direction.UP, world.dimension(), null, 0);
        var helper = new WorkerHelper(options);
        var creep = new EntityMeeCreeps(world);
        creep.addStack(new ItemStack(Items.WHEAT_SEEDS, 2));
        creep.addStack(new ItemStack(Items.NETHER_WART, 2));
        helper.setWorker(new HarvestReplantActionWorker(helper) {
            @Override
            public void tick(boolean wrapUp) {
                harvest(wheat);
                harvest(wart);
            }
        });
        helper.speedUp(0);
        helper.tick(creep, false);
        test.assertTrue(world.getBlockState(wheat).is(Blocks.WHEAT)
                && world.getBlockState(wheat).getValue(CropBlock.AGE) == 0, "Wheat did not replant");
        test.assertTrue(world.getBlockState(wart).is(Blocks.NETHER_WART)
                && world.getBlockState(wart).getValue(net.minecraft.world.level.block.NetherWartBlock.AGE) == 0, "Nether wart did not replant");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void dimensionTransitionPreservesCarriedItems(GameTestHelper test) {
        var world = test.getLevel();
        var target = world.getServer().getLevel(Level.NETHER);
        var creep = new EntityMeeCreeps(world);
        creep.moveTo(test.absoluteVec(new net.minecraft.world.phys.Vec3(3.5, 1, 3.5)));
        creep.addStack(new ItemStack(Items.DIAMOND, 3));
        world.addFreshEntity(creep);
        var moved = EntityTeleportation.teleportEntity(creep, target, 0.5, 100, 0.5, Direction.EAST);
        test.assertTrue(moved instanceof EntityMeeCreeps && moved.level() == target, "Cross-dimension teleport failed");
        var teleported = (EntityMeeCreeps) moved;
        test.assertTrue(teleported.getInventory().getFirst().is(Items.DIAMOND)
                && teleported.getInventory().getFirst().getCount() == 3, "Teleport lost inventory");
        test.assertTrue(teleported.getX() == .5 && teleported.getY() == 100 && teleported.getZ() == .5
                && teleported.getYRot() == Direction.EAST.toYRot(), "Teleport lost position or facing");
        teleported.discard();
        test.succeed();
    }
}
