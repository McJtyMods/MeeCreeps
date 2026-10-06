package mcjty.meecreeps;

import io.netty.buffer.Unpooled;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.blocks.PortalTileEntity;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.items.*;
import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.teleport.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.*;

import java.util.List;

@GameTestHolder("meecreeps")
@PrefixGameTestTemplate(false)
public class PortGameTests {
    private static void check(boolean condition, String message) {
        if (!condition)
            throw new GameTestAssertException(message);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void lightingIgnoresDaylight(GameTestHelper test) {
        var world = test.getLevel();
        world.setDayTime(6000);
        world.setWeatherParameters(6000, 0, false, false);
        world.updateSkyBrightness();
        BlockPos target = test.absolutePos(new BlockPos(3, 80, 3));
        for (BlockPos pos : BlockPos.betweenClosed(target.offset(-10, -6, -10), target.offset(10, 5, 10))) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        world.setBlock(target.below(), Blocks.STONE.defaultBlockState(), 3);
        // Enable lighting and seed daylight in this otherwise empty GameTest section.
        var lightEngine = world.getChunkSource().getLightEngine();
        lightEngine.initializeLight(world.getChunkAt(target), true);
        lightEngine.lightChunk(world.getChunkAt(target), false);
        var skylight = new net.minecraft.world.level.chunk.DataLayer();
        skylight.fill(15);
        lightEngine.queueSectionData(net.minecraft.world.level.LightLayer.SKY, net.minecraft.core.SectionPos.of(target), skylight);
        var creep = new EntityMeeCreeps(world);
        creep.addStack(new ItemStack(Items.TORCH, 2));
        var options = new ActionOptions(List.of(), List.of(), target, Direction.UP, world.dimension(), null, 0);
        boolean[] completed = {false};
        var helper = new mcjty.meecreeps.actions.workers.WorkerHelper(options) {
            @Override
            public mcjty.meecreeps.api.IMeeCreep getMeeCreep() {
                return creep;
            }

            @Override
            public void navigateTo(BlockPos pos, java.util.function.Consumer<BlockPos> job) {
                check(pos.equals(target), "Lighting selected a spot outside the test platform");
                job.accept(pos);
            }

            @Override
            public void taskIsDone() {
                completed[0] = true;
            }
        };
        var worker = new mcjty.meecreeps.actions.workers.LightupActionWorker(helper);
        var factory = new mcjty.meecreeps.actions.factories.LightupActionFactory();
        test.startSequence().thenWaitUntil(() -> {
            check(world.getMaxLocalRawBrightness(target) >= 7, "Test spot is not lit by daylight: sky="
                    + world.getBrightness(net.minecraft.world.level.LightLayer.SKY, target) + ", darken=" + world.getSkyDarken() + ", pos=" + target + ", sky=" + world.canSeeSky(target)
                    + ", hasSky=" + world.dimensionType().hasSkyLight());
        }).thenExecute(() -> {
            check(world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, target) == 0,
                    "Test spot already has artificial light");
            check(factory.isPossible(world, target, Direction.UP), "Lighting action is unavailable during daytime");
            worker.tick(false);
            check(!completed[0], "Lighting finished without placing a torch during daytime");
            check(world.getBlockState(target).is(Blocks.TORCH), "Daylight prevented torch placement");
            check(creep.getInventory().stream().filter(stack -> stack.is(Items.TORCH)).mapToInt(ItemStack::getCount).sum() == 1,
                    "Daytime lighting consumed the wrong number of torches");
        }).thenWaitUntil(() -> {
            check(world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, target) >= 7, "Torch light has not propagated");
        }).thenExecute(() -> {
            worker.tick(false);
            check(completed[0], "Lighting did not finish once the platform had artificial light");
            check(!factory.isPossible(world, target, Direction.UP), "Lighting is offered for an already lit platform");
        }).thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void groundTorchPlacement(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 1, 3));
        world.setBlock(target.below(), Blocks.STONE.defaultBlockState(), 3);
        // A nearby wall must not take priority over the floor when lighting an area.
        world.setBlock(target.north(), Blocks.STONE.defaultBlockState(), 3);
        ItemStack torches = new ItemStack(Items.TORCH, 2);
        mcjty.meecreeps.varia.BlockTools.placeStackAt(
                mcjty.meecreeps.varia.GeneralTools.getHarvester(world), torches, world, target, null);
        check(world.getBlockState(target).is(Blocks.TORCH), "Ground placement did not create a standing torch");
        check(torches.getCount() == 1, "Ground placement consumed the wrong number of torches");
        test.runAfterDelay(5, () -> {
            check(world.getBlockState(target).is(Blocks.TORCH), "Ground torch dropped after placement");
            test.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void wallTorchesCompleteHouseRequirement(GameTestHelper test) {
        var world = test.getLevel();
        var options = new ActionOptions(List.of(), List.of(), BlockPos.ZERO, Direction.UP, world.dimension(), null, 0);
        var house = new mcjty.meecreeps.actions.schematics.SchematicHouse(7,
                new mcjty.meecreeps.actions.workers.WorkerHelper(options));
        var desiredTorch = house.getDesiredBlock(new BlockPos(2, 3, 0));
        for (Direction support : Direction.Plane.HORIZONTAL) {
            BlockPos target = test.absolutePos(new BlockPos(3, 3, 3)).relative(support, 2);
            world.setBlock(target.relative(support), Blocks.STONE.defaultBlockState(), 3);
            ItemStack torches = new ItemStack(Items.TORCH, 2);
            var state = mcjty.meecreeps.varia.BlockTools.placeStackAt(
                    mcjty.meecreeps.varia.GeneralTools.getHarvester(world), torches, world, target, null);
            check(state.is(Blocks.WALL_TORCH), "Could not attach torch to " + support + " wall");
            check(state.canSurvive(world, target), "Wall torch has the wrong attachment");
            check(mcjty.meecreeps.actions.workers.WorkerHelper.isTorch(state.getBlock()),
                    "Placed wall torch is not recognized as a completed torch");
            check(desiredTorch.getStateMatcher().test(state), "House keeps requesting an already placed wall torch");
            check(torches.getCount() == 1, "Wall placement consumed the wrong number of torches");
        }
        check(!mcjty.meecreeps.actions.workers.WorkerHelper.isTorch(Blocks.REDSTONE_WALL_TORCH),
                "Redstone torches were accepted as lighting torches");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void torchPlacementDoesNotShiftOccupiedTarget(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 1, 3));
        world.setBlock(target, Blocks.STONE.defaultBlockState(), 3);
        ItemStack torch = new ItemStack(Items.TORCH);
        mcjty.meecreeps.varia.BlockTools.placeStackAt(
                mcjty.meecreeps.varia.GeneralTools.getHarvester(world), torch, world, target, null);
        check(world.isEmptyBlock(target.above()), "Torch placement shifted above the occupied target");
        check(torch.getCount() == 1, "Failed placement consumed a torch");
        test.succeed();
    }

    private static mcjty.meecreeps.api.IDesiredBlock stoneForBuilding() {
        return new mcjty.meecreeps.api.IDesiredBlock() {
            public int getAmount() {
                return 1;
            }

            public String getName() {
                return "stone";
            }

            public java.util.function.Predicate<ItemStack> getMatcher() {
                return stack -> stack.is(Items.STONE);
            }

            public java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> getStateMatcher() {
                return state -> state.is(Blocks.STONE);
            }
        };
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void buildingClearsOccupiedTarget(GameTestHelper test) {
        checkBuildingMovesAside(test, false, false);
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void buildingMovesAsideUnderLowCeiling(GameTestHelper test) {
        checkBuildingMovesAside(test, true, false);
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void buildingClearsPartialBodyOverlap(GameTestHelper test) {
        checkBuildingMovesAside(test, true, true);
    }

    @GameTest(template = "empty", timeoutTicks = 120)
    public static void buildingJumpsOnNarrowPillar(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 5, 3));
        world.setBlock(target.below(), Blocks.STONE.defaultBlockState(), 3);
        var creep = new EntityMeeCreeps(world);
        creep.moveTo(target.getX() + .5, target.getY(), target.getZ() + .5, 0, 0);
        creep.setOnGround(true);
        creep.addStack(new ItemStack(Items.STONE, 2));
        world.addFreshEntity(creep);
        var options = new ActionOptions(List.of(), List.of(), target, Direction.UP, world.dimension(), null, 0);
        var helper = new mcjty.meecreeps.actions.workers.WorkerHelper(options);
        helper.setSpeed(1);
        helper.speedUp(0);
        helper.setWorker(new mcjty.meecreeps.actions.workers.IdleActionWorker(helper) {
            private boolean started;

            @Override
            public void tick(boolean wrapUp) {
                if (!started) {
                    started = true;
                    helper.placeBuildingBlock(target, stoneForBuilding());
                    check(world.isEmptyBlock(target), "Placed before jumping clear");
                }
            }
        });
        test.onEachTick(() -> {
            helper.tick(creep, false);
            if (world.getBlockState(target).is(Blocks.STONE)) {
                check(creep.getY() >= target.getY() + 1, "Placed before clearing the target vertically");
                check(world.noCollision(creep), "Jump placed MeeCreeps inside a block");
                test.succeed();
            }
        });
    }

    private static void checkBuildingMovesAside(GameTestHelper test, boolean lowCeiling, boolean partialOverlap) {
        var world = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 1, 3));
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.setBlock(target.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
                if (lowCeiling)
                    world.setBlock(target.offset(x, 2, z), Blocks.STONE.defaultBlockState(), 3);
            }
        }
        var creep = new EntityMeeCreeps(world);
        creep.moveTo(target.getX() + (partialOverlap ? 1.1 : .5), target.getY(), target.getZ() + .5, 0, 0);
        creep.addStack(new ItemStack(Items.STONE, 2));
        world.addFreshEntity(creep);
        var options = new ActionOptions(List.of(), List.of(), target, Direction.UP, world.dimension(), null, 0);
        var helper = new mcjty.meecreeps.actions.workers.WorkerHelper(options);
        helper.setSpeed(1);
        helper.speedUp(0);
        helper.setWorker(new mcjty.meecreeps.actions.workers.IdleActionWorker(helper) {
            private boolean started;

            @Override
            public void tick(boolean wrapUp) {
                if (!started) {
                    started = true;
                    check(helper.placeBuildingBlock(target, stoneForBuilding()), "Building request rejected");
                    check(creep.getInventory().stream().filter(s -> s.is(Items.STONE)).mapToInt(ItemStack::getCount).sum() == 2,
                            "Materials consumed before MeeCreeps cleared the target");
                }
            }
        });
        test.onEachTick(() -> {
            helper.tick(creep, false);
            if (world.getBlockState(target).is(Blocks.STONE)) {
                check(!creep.getBoundingBox().intersects(new net.minecraft.world.phys.AABB(target)), "Block placed inside MeeCreeps");
                check(world.noCollision(creep), "MeeCreeps moved into an obstruction");
                check(creep.getInventory().stream().filter(s -> s.is(Items.STONE)).mapToInt(ItemStack::getCount).sum() == 1,
                        "Placement consumed the wrong number of blocks");
                test.succeed();
            }
        });
    }

    @GameTest(template = "empty")
    public static void failedBuildingPreservesMaterials(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 1, 3));
        var blocker = new EntityMeeCreeps(world);
        blocker.moveTo(target.getX() + .5, target.getY(), target.getZ() + .5, 0, 0);
        world.addFreshEntity(blocker);
        var creep = new EntityMeeCreeps(world);
        creep.moveTo(target.getX() + 3, target.getY(), target.getZ() + .5, 0, 0);
        creep.addStack(new ItemStack(Items.STONE, 2));
        var options = new ActionOptions(List.of(), List.of(), target, Direction.UP, world.dimension(), null, 0);
        var helper = new mcjty.meecreeps.actions.workers.WorkerHelper(options);
        helper.setWorker(new mcjty.meecreeps.actions.workers.IdleActionWorker(helper) {
            @Override
            public void tick(boolean wrapUp) {
                helper.placeBuildingBlock(target, stoneForBuilding());
                check(world.isEmptyBlock(target), "Placement ignored another entity");
                check(creep.getInventory().stream().filter(s -> s.is(Items.STONE)).mapToInt(ItemStack::getCount).sum() == 2,
                        "Failed placement lost materials");
                blocker.discard();
                helper.placeBuildingBlock(target, stoneForBuilding());
                check(world.getBlockState(target).is(Blocks.STONE), "Placement did not recover when obstruction cleared");
            }
        });
        helper.speedUp(0);
        helper.tick(creep, false);
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void energyAndCartridgeRecipes(GameTestHelper test) {
        ItemStack cartridge = new ItemStack(Registration.CARTRIDGE.get());
        var energy = java.util.Objects.requireNonNull(cartridge.getCapability(Capabilities.EnergyStorage.ITEM));
        check(energy.receiveEnergy(1250, true) == 1250 && energy.getEnergyStored() == 0, "Simulation changed energy");
        check(energy.receiveEnergy(1250, false) == 1250 && CartridgeItem.getCharge(cartridge) == 1, "Partial charging failed");
        cartridge = ItemStack.parseOptional(test.getLevel().registryAccess(), (CompoundTag) cartridge.save(test.getLevel().registryAccess()));
        check(java.util.Objects.requireNonNull(cartridge.getCapability(Capabilities.EnergyStorage.ITEM)).getEnergyStored() == 1250, "Energy failed NBT reload");
        var inv = new TransientCraftingContainer(new AbstractContainerMenu(null, 0) {
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            public boolean stillValid(Player player) {
                return true;
            }
        }, 2, 2);
        ItemStack emptyGun = new ItemStack(Registration.EMPTY_GUN.get());
        var destination = new TeleportDestination("Home", Level.NETHER, new BlockPos(2, 70, 3), Direction.WEST);
        PortalGunItem.addDestination(emptyGun, destination, 3);
        inv.setItem(0, emptyGun);
        inv.setItem(1, cartridge);
        var insert = new InsertCartridgeFactory(CraftingBookCategory.MISC);
        check(insert.matches(inv.asCraftInput(), test.getLevel()), "Insertion recipe didn't match");
        var gun = insert.assemble(inv.asCraftInput(), test.getLevel().registryAccess());
        check(ItemEnergy.stored(gun) == 1250, "Insertion lost partial FE");
        check(PortalGunItem.getDestinations(gun).get(3).getDimension() == Level.NETHER, "Insertion lost destinations");
        inv.clearContent();
        inv.setItem(0, gun);
        var remove = new RemoveCartridgeFactory(CraftingBookCategory.MISC);
        check(remove.matches(inv.asCraftInput(), test.getLevel()), "Removal recipe didn't match");
        check(ItemEnergy.stored(remove.assemble(inv.asCraftInput(), test.getLevel().registryAccess())) == 1250, "Removal lost partial FE");
        check(remove.getRemainingItems(inv.asCraftInput()).get(0).is(Registration.EMPTY_GUN.get()), "Removal didn't return empty gun");
        inv.setItem(1, new ItemStack(Items.DIAMOND));
        check(!remove.matches(inv.asCraftInput(), test.getLevel()), "Removal accepted extra items");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void actionsAndEntityPersistence(GameTestHelper test) {
        var options = new ActionOptions(List.of(new MeeCreepActionType("meecreeps.idle")), List.of(), new BlockPos(4, -40, 7), Direction.DOWN, Level.NETHER, null, 42);
        options.setTask(new MeeCreepActionType("meecreeps.idle"), null);
        options.setStage(Stage.WORKING);
        options.registerDrops(new BlockPos(1, -30, 2), List.of(new ItemStack(Items.DIAMOND, 3)));
        CompoundTag tag = new CompoundTag();
        options.writeToNBT(tag, test.getLevel().registryAccess());
        var restored = new ActionOptions(tag, test.getLevel().registryAccess());
        check(restored.getDimension() == Level.NETHER && restored.getDrops().get(0).getValue().getCount() == 3, "Action NBT lost dimension or drops");
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            options.writeToBuf(buffer);
            var client = new ActionOptions(buffer);
            check(client.getDimension() == Level.NETHER && client.getActionId() == 42 && client.getFurtherQuestionId() == null, "Action packet roundtrip failed");
        } finally {
            buffer.release();
        }
        EntityMeeCreeps creep = new EntityMeeCreeps(test.getLevel());
        creep.setHeldBlockState(Blocks.OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
        creep.addStack(new ItemStack(Items.DIAMOND, 5));
        creep.setVariationFace(4);
        CompoundTag entityTag = new CompoundTag();
        creep.addAdditionalSaveData(entityTag);
        EntityMeeCreeps loaded = new EntityMeeCreeps(test.getLevel());
        loaded.readAdditionalSaveData(entityTag);
        check(loaded.getHeldBlockState().getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS) == Direction.Axis.X, "Carried block properties lost");
        check(loaded.getInventory().get(0).getCount() == 5 && loaded.getVariationFace() == 4, "Inventory or face lost");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void wallPortalsKeepAimedHeight(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos aimed = test.absolutePos(new BlockPos(3, 2, 3));
        world.setBlock(aimed, Blocks.BIRCH_LOG.defaultBlockState(), 3);
        world.setBlock(aimed.below(), Blocks.BIRCH_LOG.defaultBlockState(), 3);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos expected = aimed.relative(side);
            world.setBlock(expected.below(2), Blocks.STONE.defaultBlockState(), 3);
            check(expected.equals(TeleportationTools.findBestPosition(world, aimed, side)),
                    "Wall portal moved below the aimed face: " + side);
            world.setBlock(expected, Blocks.STONE.defaultBlockState(), 3);
            check(TeleportationTools.findBestPosition(world, aimed, side) == null,
                    "Wall portal would replace a solid block: " + side);
            world.removeBlock(expected, false);
            world.setBlock(expected.above(), Blocks.STONE.defaultBlockState(), 3);
            check(TeleportationTools.findBestPosition(world, aimed, side) == null,
                    "Wall portal accepted obstructed headroom: " + side);
            world.removeBlock(expected.above(), false);
            world.removeBlock(expected.below(2), false);
            check(expected.equals(TeleportationTools.findBestPosition(world, aimed, side)),
                    "Wall portal required a floor below the aimed face: " + side);
        }
        test.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void portalPairExpires(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos source = test.absolutePos(new BlockPos(1, 1, 1)), destination = test.absolutePos(new BlockPos(5, 1, 5));
        world.setBlock(source.below(), Blocks.STONE.defaultBlockState(), 3);
        world.setBlock(destination.below(), Blocks.STONE.defaultBlockState(), 3);
        TeleportationTools.makePortalPair(world, source.below(), Direction.UP, new TeleportDestination("Test", world.dimension(), destination, Direction.UP));
        check(world.getBlockEntity(source) instanceof PortalTileEntity, "Source portal not created");
        check(world.getBlockEntity(destination) instanceof PortalTileEntity, "Destination portal not created");
        var portal = (PortalTileEntity) world.getBlockEntity(source);
        var restored = new PortalTileEntity(source, world.getBlockState(source));
        restored.loadWithComponents(portal.saveWithFullMetadata(world.registryAccess()), world.registryAccess());
        check(restored.getPortalSide() == Direction.UP && restored.getTimeout() > 0, "Portal NBT reload failed");
        portal.setTimeout(2);
        test.runAfterDelay(5, () -> {
            check(!world.getBlockState(source).is(Registration.PORTAL.get()) && !world.getBlockState(destination).is(Registration.PORTAL.get()), "Portal pair did not expire together");
            test.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void harvestingHonorsProtection(GameTestHelper test) {
        var world = test.getLevel();
        var pos = test.absolutePos(new BlockPos(2, 1, 2));
        world.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        var options = new ActionOptions(List.of(), List.of(), pos, Direction.UP, world.dimension(), null, 0);
        var helper = new mcjty.meecreeps.actions.workers.WorkerHelper(options);
        var creep = new EntityMeeCreeps(world);
        helper.setWorker(new mcjty.meecreeps.actions.workers.IdleActionWorker(helper) {
            @Override
            public void tick(boolean wrapUp) {
                java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.BreakEvent> deny = e -> {
                    if (e.getPos().equals(pos))
                        e.setCanceled(true);
                };
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(deny);
                try {
                    check(!helper.harvestAndPickup(pos), "Canceled break was allowed");
                } finally {
                    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);
                }
                check(world.getBlockState(pos).is(Blocks.STONE), "Protected block was removed");
                check(helper.harvestAndPickup(pos), "Permitted break failed");
                check(world.isEmptyBlock(pos), "Harvest didn't remove block");
                check(creep.hasItem(stack -> stack.is(Items.COBBLESTONE)), "Harvest lost loot table drops");
            }
        });
        helper.speedUp(0);
        helper.tick(creep, false);
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void movingBlockPreservesInventory(GameTestHelper test) {
        var world = test.getLevel();
        BlockPos source = test.absolutePos(new BlockPos(2, 1, 2)), destination = test.absolutePos(new BlockPos(5, 1, 5));
        world.setBlock(source, Blocks.CHEST.defaultBlockState(), 3);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) world.getBlockEntity(source);
        chest.setItem(4, new ItemStack(Items.DIAMOND, 7));
        var creep = new EntityMeeCreeps(world);
        creep.setHeldBlockState(world.getBlockState(source));
        creep.setCarriedNBT(chest.saveWithFullMetadata(world.registryAccess()));
        world.removeBlockEntity(source);
        world.removeBlock(source, false);
        creep.placeDownBlock(destination);
        var moved = (net.minecraft.world.level.block.entity.ChestBlockEntity) world.getBlockEntity(destination);
        check(moved != null && moved.getItem(4).is(Items.DIAMOND) && moved.getItem(4).getCount() == 7, "Moved chest lost inventory");
        check(creep.getHeldBlockState() == null && creep.getCarriedNBT() == null, "Carried chest wasn't released");
        test.succeed();
    }

}
