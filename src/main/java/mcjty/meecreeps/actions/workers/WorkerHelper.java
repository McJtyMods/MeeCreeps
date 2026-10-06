package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.setup.Registration;
import mcjty.meecreeps.varia.BlockTools;
import mcjty.meecreeps.varia.SoundTools;
import mcjty.meecreeps.FabricEventHandlers;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.*;
import mcjty.meecreeps.api.*;
import mcjty.meecreeps.config.ConfigSetup;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.items.CreepCubeItem;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.InventoryTools;
import mcjty.meecreeps.varia.ChestAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WorkerHelper implements IWorkerHelper {

    private final double DISTANCE_TOLERANCE = 1.4;

    private IActionWorker worker;
    private final ActionOptions options;
    private EntityMeeCreeps entity;
    private boolean needsToPutAway = false;
    private int waitABit = 10;
    private int speed = 10;

    private BlockPos movingToPos;
    private Entity movingToEntity;

    // To detect if we're stuck
    private double prevPosX;
    private double prevPosY;
    private double prevPosZ;
    private int stuckCounter;

    private int pathTries = 0;
    private Consumer<BlockPos> job;
    private Runnable delayedJob;
    private int delayedTicks;
    private List<ItemEntity> itemsToPickup = new ArrayList<>();
    private BlockPos materialChest;

    // While building or flattening this will contain positions that we want to skip because they are too hard or unbreakable
    private Set<BlockPos> positionsToSkip = new HashSet<>();

    private String lastMessage = "";
    private String[] lastMessageParameters = new String[0];

    public WorkerHelper(IActionContext options) {
        this.options = (ActionOptions) options;
    }

    private static final Set<String> TORCHES = new HashSet<>();

    static {
        TORCHES.add("minecraft:torch");
        TORCHES.add("tconstruct:stone_torch");
        TORCHES.add("integrateddynamics:menril_torch");
        TORCHES.add("integrateddynamics:menril_torch_stone");
    }

    public static boolean isTorch(ItemStack stack) {
        return TORCHES.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    public static boolean isTorch(Block block) {
        // Standing and wall variants share an item, but have different block IDs.
        return TORCHES.contains(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString())
                || TORCHES.contains(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(block.asItem()).toString());
    }

    public void setWorker(IActionWorker worker) {
        this.worker = worker;
    }

    public IActionWorker getWorker() {
        return worker;
    }

    public void cancelJob() {
        job = null;
        delayedJob = null;
    }

    @Override
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public IActionContext getContext() {
        return options;
    }

    @Override
    public IMeeCreep getMeeCreep() {
        return entity;
    }

    private static final IDesiredBlock AIR = new IDesiredBlock() {
        @Override
        public String getName() {
            return "air";
        }

        @Override
        public int getAmount() {
            return 0;
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return ItemStack::isEmpty;
        }

        @Override
        public Predicate<BlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() == Blocks.AIR;
        }
    };

    private static final IDesiredBlock IGNORE = new IDesiredBlock() {
        @Override
        public String getName() {
            return "IGNORE";
        }

        @Override
        public int getAmount() {
            return 0;
        }

        @Override
        public int getPass() {
            return -1;          // That way this is ignored
        }

        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return stack -> false;
        }

        @Override
        public Predicate<BlockState> getStateMatcher() {
            return blockState -> false;
        }
    };

    @Override
    public IDesiredBlock getAirBlock() {
        return AIR;
    }

    @Override
    public IDesiredBlock getIgnoreBlock() {
        return IGNORE;
    }

    /**
     * Returns absolute position
     */
    @Override
    public BlockPos findSpotToFlatten(@NonNull IBuildSchematic schematic) {
        BlockPos tpos = options.getTargetPos();
        BlockPos minPos = schematic.getMinPos();
        BlockPos maxPos = schematic.getMaxPos();

        List<BlockPos> todo = new ArrayList<>();
        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int y = minPos.getY(); y <= maxPos.getY(); y++) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                    BlockPos relativePos = BlockPos.containing(x, y, z);
                    BlockPos p = tpos.offset(relativePos);
                    BlockState state = entity.getWorld().getBlockState(p);
                    IDesiredBlock desired = schematic.getDesiredBlock(relativePos);
                    if (desired != IGNORE) {
                        if (!desired.getStateMatcher().test(state) && !entity.getWorld().isEmptyBlock(p) && !positionsToSkip.contains(p)) {
                            todo.add(p);
                        }
                    }
                }
            }
        }
        if (todo.isEmpty()) {
            return null;
        }

        BlockPos position = entity.getEntity().blockPosition();
        todo.sort((o1, o2) -> {
            double d1 = position.distSqr(o1);
            double d2 = position.distSqr(o2);
            return Double.compare(d1, d2);
        });
        return todo.get(0);
    }

    /**
     * Return the relative spot to build
     */
    @Override
    public BlockPos findSpotToBuild(@NonNull IBuildSchematic schematic, @NonNull BuildProgress progress, @NonNull Set<BlockPos> toSkip) {
        BlockPos tpos = options.getTargetPos();
        BlockPos minPos = schematic.getMinPos();
        BlockPos maxPos = schematic.getMaxPos();

        List<BlockPos> todo = new ArrayList<>();
        for (int x = minPos.getX(); x <= maxPos.getX(); x++) {
            for (int z = minPos.getZ(); z <= maxPos.getZ(); z++) {
                BlockPos relativePos = new BlockPos(x, progress.getHeight(), z);
                if (!toSkip.contains(relativePos)) {
                    BlockPos p = tpos.offset(relativePos);
                    BlockState state = entity.getWorld().getBlockState(p);
                    IDesiredBlock desired = schematic.getDesiredBlock(relativePos);
                    if (desired.getPass() == progress.getPass() && !desired.getStateMatcher().test(state) && !positionsToSkip.contains(p)) {
                        todo.add(relativePos);
                    }
                }
            }
        }
        if (todo.isEmpty()) {
            if (!progress.next(schematic)) {
                return null;    // Done
            }
            return findSpotToBuild(schematic, progress, toSkip);
        }
        BlockPos position = entity.getEntity().blockPosition().subtract(tpos);        // Make entity position relative for distance calculation
        todo.sort((o1, o2) -> {
            double d1 = position.distSqr(o1);
            double d2 = position.distSqr(o2);
            return Double.compare(d1, d2);
        });
        return todo.get(0);
    }

    @Override
    public void delayForHardBlocks(BlockPos pos, Consumer<BlockPos> nextJob) {
        Level world = entity.level();
        if (world.isEmptyBlock(pos)) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        if (!allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world))) {
            return;
        }
        Block block = state.getBlock();
        if (block instanceof LiquidBlock) {
            nextJob.accept(pos);
        } else {
            float hardness = state.getDestroySpeed(world, pos);
            if (hardness < ConfigSetup.delayAtHardness.get()) {
                nextJob.accept(pos);
            } else {
                delay((int) (hardness * ConfigSetup.delayFactor.get()), () -> nextJob.accept(pos));
            }
        }
    }

    @Override
    public boolean handleFlatten(@NonNull IBuildSchematic schematic) {
        BlockPos flatSpot = findSpotToFlatten(schematic);
        if (flatSpot == null) {
            return false;
        } else {
            BlockPos navigate = findBestNavigationSpot(flatSpot);
            if (navigate != null) {
                navigateTo(navigate, p -> {
                    delayForHardBlocks(flatSpot, pp -> {
                        if (!harvestAndDrop(flatSpot)) {
                            positionsToSkip.add(flatSpot);
                        }
                    });
                });
            } else {
                // We couldn't reach it. Just drop the block
                delayForHardBlocks(flatSpot, pp -> {
                    if (!harvestAndDrop(flatSpot)) {
                        positionsToSkip.add(flatSpot);
                    }
                });
            }
            return true;
        }
    }

    @Override
    public boolean handleBuilding(@NonNull IBuildSchematic schematic, @NonNull BuildProgress progress, @NonNull Set<BlockPos> toSkip) {
        BlockPos relativePos = findSpotToBuild(schematic, progress, toSkip);
        if (relativePos != null) {
            IDesiredBlock desired = schematic.getDesiredBlock(relativePos);
            if (!entity.hasItem(desired.getMatcher())) {
                if (entity.hasRoom(desired.getMatcher())) {
                    if (desired.isOptional()) {
                        if (!findItemOnGroundOrInChest(desired.getMatcher(), desired.getAmount())) {
                            // We don't have any of these. Just skip them
                            toSkip.add(relativePos);
                        }
                    } else {
                        findItemOnGroundOrInChest(desired.getMatcher(), desired.getAmount(), "message.meecreeps.cannot_find", desired.getName());
                    }
                } else {
                    // First put away stuff
                    putStuffAway();
                }
            } else {
                // Supplies are available again, so report a later shortage even
                // if it concerns the same material as before.
                lastMessage = "";
                lastMessageParameters = new String[0];
                BlockPos buildPos = relativePos.offset(options.getTargetPos());
                BlockPos navigate = findBestNavigationSpot(buildPos);
                if (navigate != null) {
                    navigateTo(navigate, p -> {
                        if (!placeBuildingBlock(buildPos, desired)) {
                            positionsToSkip.add(buildPos);
                        }
                    });
                } else {
                    // We couldn't reach it. Just build the block
                    if (!placeBuildingBlock(buildPos, desired)) {
                        positionsToSkip.add(buildPos);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void giveDropsToMeeCreeps(@NonNull List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            ItemStack remaining = entity.addStack(stack);
            if (!remaining.isEmpty()) {
                itemsToPickup.add(entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), remaining, 0.0f));
                needsToPutAway = true;
            }
        }
    }

    @Override
    public void showMessage(String message, String... parameters) {
        if (lastMessage.equals(message) && Arrays.equals(lastMessageParameters, parameters)) {
            return;
        }
        ServerPlayer player = getPlayer();
        if (player != null) {
            lastMessage = message;
            lastMessageParameters = parameters.clone();
            sendMessageToPlayer(player, message, parameters);
        }
    }

    protected void sendMessageToPlayer(ServerPlayer player, String message, String... parameters) {
        MeeCreepsMessages.INSTANCE.sendTo(new PacketShowBalloonToClient(message, parameters), player);
    }

    @Override
    public void registerHarvestableBlock(BlockPos pos) {
        FabricEventHandlers.trackHarvest(entity.getWorld(), pos, options.getActionId());
    }

    @Override
    public void navigateTo(BlockPos pos, Consumer<BlockPos> job) {
        double d = getSquareDist(entity, pos);
        if (d < DISTANCE_TOLERANCE) {
            job.accept(pos);
        } else if (!entity.getNavigation().moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 2.0)) {
            // We need to teleport
            entity.teleportTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
            job.accept(pos);
        } else {
            this.movingToPos = pos;
            this.movingToEntity = null;
            pathTries = 1;
            this.job = job;
            prevPosX = entity.getX();
            prevPosY = entity.getY();
            prevPosZ = entity.getZ();
            stuckCounter = 0;
//            prevPosX = entity.getX();
        }
    }

    @Override
    public boolean navigateTo(Entity dest, Consumer<BlockPos> job, double maxDist) {
        if (dest == null || dest.isRemoved()) {
            return false;
        }
        double d = getSquareDist(entity, dest);
        if (d > maxDist * maxDist) {
            return false;
        } else if (d < DISTANCE_TOLERANCE) {
            job.accept(dest.blockPosition());
        } else if (!entity.getNavigation().moveTo(dest, 2.0)) {
            // We need to teleport
            entity.teleportTo(dest.getX(), dest.getY(), dest.getZ());
            job.accept(dest.blockPosition());
        } else {
            this.movingToPos = null;
            this.movingToEntity = dest;
            pathTries = 1;
            this.job = job;
        }
        return true;
    }

    private static double getSquareDist(Entity source, BlockPos dest) {
        double d0 = dest.distToCenterSqr(source.getX(), source.getY() - 1, source.getZ());
        double d1 = dest.distToCenterSqr(source.getX(), source.getY(), source.getZ());
        double d2 = dest.distToCenterSqr(source.getX(), source.getY() + source.getEyeHeight(), source.getZ());
        return Math.min(Math.min(d0, d1), d2);
    }

    private static double getSquareDist(Entity source, Entity dest) {
        Vec3 lowPosition = new Vec3(source.getX(), source.getY() - 1, source.getZ());
        Vec3 position = new Vec3(source.getX(), source.getY(), source.getZ());
        Vec3 eyePosition = new Vec3(source.getX(), source.getY() + source.getEyeHeight(), source.getZ());
        double d0 = lowPosition.distanceToSqr(dest.getX(), dest.getY(), dest.getZ());
        double d1 = position.distanceToSqr(dest.getX(), dest.getY(), dest.getZ());
        double d2 = eyePosition.distanceToSqr(dest.getX(), dest.getY(), dest.getZ());
        return Math.min(Math.min(d0, d1), d2);
    }

    @Override
    public boolean navigateTo(Entity dest, Consumer<BlockPos> job) {
        return navigateTo(dest, job, 1000000000);
    }

    private boolean isStuck() {
        return Math.abs(entity.getX() - prevPosX) < 0.01 && Math.abs(entity.getY() - prevPosY) < 0.01 && Math.abs(entity.getZ() - prevPosZ) < 0.01;
    }

    private boolean isCube(ItemStack stack) {
        return stack.getItem() instanceof CreepCubeItem;
    }

    @Override
    public void delay(int ticks, Runnable task) {
        delayedTicks = ticks;
        delayedJob = task;
    }

    public void tick(EntityMeeCreeps entity, boolean timeToWrapUp) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = speed;

        this.entity = entity;
        if (delayedJob != null) {
            delayedTicks -= speed;
            if (delayedTicks < 0) {
                Runnable d = this.delayedJob;
                delayedJob = null;
                d.run();
            }
        } else if (job != null) {
            handleJob();
        } else if (entity.hasItem(this::isCube)) {
            spawnAngryCreep();
        } else if (findMeeCreepBoxOnGround()) {
            entity.dropInventory();
            setSpeed(20);
        } else if (!options.getDrops().isEmpty()) {
            handleDropCollection();
        } else if (needToFindChest(timeToWrapUp)) {
            handlePutAway();
        } else if (!itemsToPickup.isEmpty()) {
            tryFindingItemsToPickup();
        } else {
            worker.tick(timeToWrapUp);
        }
        this.entity = null;
    }

    private void spawnAngryCreep() {
        entity.setHeldBlockState(Registration.CUBE.get().defaultBlockState());
        entity.setVariationFace(1);
        ServerActionManager manager = ServerActionManager.getManager();
        Level world = entity.getWorld();

        int cnt = ((net.minecraft.server.level.ServerLevel) world).getEntities(Registration.CREEP.get(), e -> true).size();
        if (cnt >= ConfigSetup.maxSpawnCount.get()) {
            return;
        }

        net.minecraft.util.RandomSource r = entity.getRandom();
        BlockPos targetPos = BlockPos.containing(entity.getX() + r.nextFloat() * 8 - 4, entity.getY(), entity.getZ() + r.nextFloat() * 8 - 4);
        int actionId = manager.createActionOptions(world, targetPos, Direction.UP, getPlayer());
        ActionOptions.spawn(world, targetPos, Direction.UP, actionId, false);
        manager.performAction(null, actionId, new MeeCreepActionType("meecreeps.angry"), null);
    }

    private void handlePutAway() {
        if (!findChestToPutItemsIn()) {
            if (!navigateTo(getPlayer(), (p) -> giveToPlayerOrDrop(), 12)) {
                entity.dropInventory();
            }
        }
        needsToPutAway = false;
    }

    private void handleDropCollection() {
        // There are drops we need to collect first.
        for (Pair<BlockPos, ItemStack> pair : options.getDrops()) {
            ItemStack drop = pair.getValue();
            if (!drop.isEmpty()) {
                ItemStack remaining = entity.addStack(drop);
                if (!remaining.isEmpty()) {
                    entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), remaining, 0.0f);
                    needsToPutAway = true;
                }
            }
        }
        options.clearDrops();
        ServerActionManager.getManager().save();
        waitABit = 1;   // Process faster
    }

    private void handleJob() {
        if (movingToEntity != null) {
            if (movingToEntity.isRemoved()) {
                job = null;
            } else {
                double d = getSquareDist(entity, movingToEntity);
                if (d < DISTANCE_TOLERANCE) {
                    job.accept(movingToEntity.blockPosition());
                    job = null;
                } else if (entity.getNavigation().isDone()) {
                    if (pathTries > 2) {
                        entity.teleportTo(movingToEntity.getX(), movingToEntity.getY(), movingToEntity.getZ());
                        job.accept(movingToEntity.blockPosition());
                        job = null;
                    } else {
                        pathTries++;
                        entity.getNavigation().moveTo(movingToEntity, 2.0);
                        stuckCounter = 0;
                    }
                } else if (isStuck()) {
                    stuckCounter++;
                    if (stuckCounter > 5) {
                        entity.teleportTo(movingToEntity.getX(), movingToEntity.getY(), movingToEntity.getZ());
                        job.accept(movingToEntity.blockPosition());
                        job = null;
                    }
                }
            }
        } else {
            double d = getSquareDist(entity, movingToPos);
            if (d < DISTANCE_TOLERANCE) {
                job.accept(movingToPos);
                job = null;
            } else if (entity.getNavigation().isDone()) {
                if (pathTries > 2) {
                    entity.teleportTo(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5);
                    job.accept(movingToPos);
                    job = null;
                } else {
                    pathTries++;
                    entity.getNavigation().moveTo(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5, 2.0);
                    stuckCounter = 0;
                }
            } else if (isStuck()) {
                stuckCounter++;
                if (stuckCounter > 5) {
                    entity.teleportTo(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5);
                    job.accept(movingToPos);
                    job = null;
                }
            }
        }
        prevPosX = entity.getX();
        prevPosY = entity.getY();
        prevPosZ = entity.getZ();
    }

    @Override
    public boolean placeBuildingBlock(BlockPos pos, IDesiredBlock desiredBlock) {
        Level world = entity.getWorld();
        if (!world.isEmptyBlock(pos) && !world.getBlockState(pos).canBeReplaced()) {
            if (!allowedToHarvest(world.getBlockState(pos), world, pos, GeneralTools.getHarvester(world))) {
                return false;
            }
            delayForHardBlocks(pos, pp -> {
                harvestAndDrop(pos);
                reallyPlace(pos, desiredBlock, world);
            });
        } else {
            reallyPlace(pos, desiredBlock, world);
        }
        return true;
    }

    private void reallyPlace(BlockPos pos, IDesiredBlock desiredBlock, Level world) {
        reallyPlace(pos, desiredBlock, world, 0);
    }

    private void reallyPlace(BlockPos pos, IDesiredBlock desiredBlock, Level world, int moveAttempts) {
        // Navigation's arrival tolerance can leave us inside the block we want to place.
        // Check the whole body, including overlap with a neighbouring block, before using materials.
        if (entity.getBoundingBox().intersects(new AABB(pos))) {
            BlockPos spot = findPlacementSpot(pos);
            if (spot == null) {
                double rise = pos.getY() + 1.0 - entity.getY();
                if (moveAttempts < 6 && rise > 0 && rise <= 1.25
                        && world.noCollision(entity, entity.getBoundingBox().expandTowards(0, rise, 0))) {
                    entity.getNavigation().stop();
                    entity.getJumpControl().jump();
                    delay(1, () -> reallyPlace(pos, desiredBlock, world, moveAttempts + 1));
                    return;
                }
                positionsToSkip.add(pos);
                return;
            }
            if (moveAttempts >= 6 || !entity.getNavigation().moveTo(spot.getX() + .5, spot.getY(), spot.getZ() + .5, 2.0)) {
                // Use the same fallback as normal navigation, but only at a checked, clear position.
                entity.teleportTo(spot.getX() + .5, spot.getY(), spot.getZ() + .5);
            }
            delay(1, () -> reallyPlace(pos, desiredBlock, world, moveAttempts + 1));
            return;
        }
        entity.getNavigation().stop();
        ItemStack blockStack = entity.consumeItem(desiredBlock.getMatcher(), 1);
        if (!blockStack.isEmpty()) {
            placeStackAt(blockStack, world, pos);
            // BlockItem consumes the item only on success. Return unused materials on failure.
            if (!blockStack.isEmpty()) {
                ItemStack remaining = entity.addStack(blockStack);
                if (!remaining.isEmpty()) {
                    itemsToPickup.add(entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), remaining, 0.0f));
                }
            }
        }
    }

    @Nullable
    private BlockPos findPlacementSpot(BlockPos target) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos spot = target.offset(dx, dy, dz);
                    AABB body = entity.getBoundingBox().move(spot.getX() + .5 - entity.getX(),
                            spot.getY() - entity.getY(), spot.getZ() + .5 - entity.getZ());
                    if (body.intersects(new AABB(target))
                            || !entity.level().getBlockState(spot.below()).isFaceSturdy(entity.level(), spot.below(), Direction.UP)
                            || !entity.level().noCollision(entity, body)) {
                        continue;
                    }
                    double distance = entity.position().distanceToSqr(spot.getX() + .5, spot.getY(), spot.getZ() + .5);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = spot;
                    }
                }
            }
        }
        return best;
    }

    @Override
    public void placeStackAt(ItemStack blockStack, Level world, BlockPos pos) {
        // BlockItem already plays the placement sound when placement succeeds.
        BlockTools.placeStackAt(GeneralTools.getHarvester(world), blockStack, world, pos, null);
    }

    @Override
    public boolean harvestAndPickup(BlockPos pos) {
        Level world = entity.level();
        if (world.isEmptyBlock(pos)) {
            return true;
        }
        BlockState state = world.getBlockState(pos);
        if (!allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world))) {
            return false;
        }
        Block block = state.getBlock();
        List<ItemStack> drops = mcjty.meecreeps.varia.BlockTools.getDrops(world, pos, state);
        SoundTools.playSound(world, state.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        block.playerWillDestroy(world, pos, state, GeneralTools.getHarvester(world));
        entity.level().removeBlock(pos, false);
        giveDropsToMeeCreeps(drops);
        return true;
    }

    @Override
    public boolean harvestAndDrop(BlockPos pos) {
        Level world = entity.level();
        if (world.isEmptyBlock(pos)) {
            return true;
        }
        BlockState state = world.getBlockState(pos);
        if (!allowedToHarvest(state, world, pos, GeneralTools.getHarvester(world))) {
            return false;
        }

        Block block = state.getBlock();

        List<ItemStack> drops = mcjty.meecreeps.varia.BlockTools.getDrops(world, pos, state);
        SoundTools.playSound(world, state.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        block.playerWillDestroy(world, pos, state, GeneralTools.getHarvester(world));
        entity.level().removeBlock(pos, false);
        for (ItemStack stack : drops) {
            entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), stack, 0.0f);
        }
        return true;
    }

    @Override
    public void pickup(ItemEntity item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.discard();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }

    @Override
    public boolean allowedToHarvest(BlockState state, Level world, BlockPos pos, Player entityPlayer) {
        if (state.getDestroySpeed(world, pos) < 0) {
            return false;
        }
        if (!(world instanceof net.minecraft.server.level.ServerLevel level) || !world.mayInteract(entityPlayer, pos)) {
            return false;
        }
        if (!net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(world, entityPlayer, pos, state, world.getBlockEntity(pos))) {
            return false;
        }
        return entityPlayer.hasCorrectToolForDrops(state);
    }

    @Override
    public void done() {
        options.setStage(Stage.DONE);
        ServerActionManager.getManager().save();
    }

    // Indicate the task is done and that it is time to do the last task (putting back stuff etc)
    @Override
    public void taskIsDone() {
        options.setStage(Stage.TASK_IS_DONE);
        ServerActionManager.getManager().save();
    }

    @Override
    public void putStuffAway() {
        needsToPutAway = true;
    }

    @Override
    public void speedUp(int t) {
        waitABit = t;
    }

    @Override
    public void dropAndPutAwayLater(ItemStack stack) {
        ItemEntity entityItem = entity.getEntity().spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), stack, 0.0f);
        itemsToPickup.add(entityItem);
        putStuffAway();
    }

    @Override
    public BlockPos findSuitablePositionNearPlayer(double distance) {
        return findSuitablePositionNearPlayer(this.entity, options.getPlayer(), distance);
    }

    public static BlockPos findSuitablePositionNearPlayer(@NonNull EntityMeeCreeps meeCreep, @NonNull Player player, double distance) {
        Vec3 playerPos = player.position();
        Vec3 entityPos = meeCreep.position();

        if (entityPos.distanceTo(playerPos) < (distance * 1.2)) {
            // No need to move
            return meeCreep.blockPosition();
        }

        double dx = playerPos.x - entityPos.x;
        double dy = playerPos.y - entityPos.y;
        double dz = playerPos.z - entityPos.z;
        Vec3 v = new Vec3(-dx, -dy, -dz);
        v = v.normalize();
        Vec3 pos = new Vec3(playerPos.x + v.x * distance, playerPos.y + v.y * distance, playerPos.z + v.z * distance);
        // First find a good spot at the specific location
        Level world = player.level();

        float width = meeCreep.getBbWidth();
        float eyeHeight = meeCreep.getEyeHeight();

        // First try on the prefered spot
        BlockPos p = scanSuitablePos(BlockPos.containing(pos.x, pos.y + .5, pos.z), world, width, eyeHeight);
        if (p != null)
            return p;
        // No good spot to stand on found. Try other spots around the prefered spot
        p = scanAround(pos, world, width, eyeHeight);
        if (p != null)
            return p;
        // No good spot to stand on found. Try other spots around the player
        p = scanAround(playerPos, world, width, eyeHeight);
        if (p != null)
            return p;

        // If all else fails we go stand where the player is
        return player.blockPosition();
    }

    private static BlockPos scanAround(Vec3 vec, Level world, float width, float eyeHeight) {
        BlockPos pos = BlockPos.containing(vec.x, vec.y + 0.5, vec.z);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = pos.offset(dx, 0, dz);
                p = scanSuitablePos(p, world, width, eyeHeight);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }

    private static BlockPos scanSuitablePos(BlockPos pos, Level world, float width, float eyeHeight) {
        for (int d = 0; d < 6; d++) {
            BlockPos p = pos.below(d);
            if (isSuitableStandingPos(world, p, width, eyeHeight)) {
                return p;
            }
            p = pos.above(d);
            if (isSuitableStandingPos(world, p, width, eyeHeight)) {
                return p;
            }
        }
        return null;
    }

    private static boolean isSuitableStandingPos(Level world, BlockPos p, float width, float eyeHeight) {
        return canStandOn(world.getBlockState(p.below()))
                && !canStandOn(world.getBlockState(p))
                && !willSuffocateHere(world, p.getX() + .5, p.getY(), p.getZ() + .5, width, eyeHeight);
    }

    private static boolean canStandOn(BlockState state) {
        return state.isSolid();
    }

    private static boolean willSuffocateHere(Level world, double posX, double posY, double posZ, float width, float eyeHeight) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; ++i) {
            int x = Mth.floor(posX + ((((i >> 1) % 2) - 0.5F) * width * 0.8F));
            int y = Mth.floor(posY + ((((i >> 0) % 2) - 0.5F) * 0.1F) + eyeHeight);
            int z = Mth.floor(posZ + ((((i >> 2) % 2) - 0.5F) * width * 0.8F));

            if (mutableBlockPos.getX() != x || mutableBlockPos.getY() != y || mutableBlockPos.getZ() != z) {
                mutableBlockPos.set(x, y, z);

                if (world.getBlockState(mutableBlockPos).isSuffocating(world, mutableBlockPos)) {

                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void giveToPlayerOrDrop() {
        ServerPlayer player = getPlayer();
        BlockPos position = entity.blockPosition();
        if (player == null || position.distSqr(player.blockPosition()) > 2 * 2) {
            if (player != null) {
                showMessage("message.meecreeps.where_are_you");
            }
            entity.dropInventory();
        } else {
            showMessage("message.meecreeps.i_gave_some_things");
            List<ItemStack> remaining = new ArrayList<>();
            for (ItemStack stack : entity.getInventory()) {
                if (!stack.isEmpty()) {
                    if (!player.getInventory().add(stack)) {
                        remaining.add(stack);
                    }
                }
            }
            player.containerMenu.broadcastChanges();
            for (ItemStack stack : remaining) {
                entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), stack, 0.0f);
            }
            entity.getInventory().clear();
        }

    }

    @Nullable
    protected ServerPlayer getPlayer() {
        return (ServerPlayer) options.getPlayer();
    }

    @Override
    public boolean findItemOnGroundOrInChest(Predicate<ItemStack> matcher, int maxAmount, String message, String... parameters) {
        List<BlockPos> meeCreepChests = findMeeCreepChests(worker.getSearchBox());
        if (meeCreepChests.isEmpty()) {
            if (!findItemOnGround(worker.getSearchBox(), matcher, this::pickup)) {
                if (!findInventoryContainingMost(worker.getSearchBox(), matcher, p -> fetchFromInventory(p, matcher, maxAmount))) {
                    showMessage(message, parameters);
                    return false;
                }
            }
        } else {
            if (!findInventoryContainingMost(meeCreepChests, matcher, p -> fetchFromInventory(p, matcher, maxAmount))) {
                showMessage(message, parameters);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean findItemOnGroundOrInChest(Predicate<ItemStack> matcher, int maxAmount) {
        List<BlockPos> meeCreepChests = findMeeCreepChests(worker.getSearchBox());
        if (meeCreepChests.isEmpty()) {
            if (!findItemOnGround(worker.getSearchBox(), matcher, this::pickup)) {
                if (!findInventoryContainingMost(worker.getSearchBox(), matcher, p -> fetchFromInventory(p, matcher, maxAmount))) {
                    return false;
                }
            }
        } else {
            if (!findInventoryContainingMost(meeCreepChests, matcher, p -> fetchFromInventory(p, matcher, maxAmount))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find all chests that have an item frame attached to them with an meecreep cube in them
     */
    private List<BlockPos> findMeeCreepChests(AABB box) {
        List<ItemFrame> frames = entity.level().getEntitiesOfClass(ItemFrame.class, box, input -> {
            if (!input.getItem().isEmpty() && input.getItem().getItem() instanceof CreepCubeItem) {
                BlockPos position = input.getPos().relative(input.getDirection().getOpposite());
                if (InventoryTools.isInventory(entity.level(), position)) {
                    return true;
                }
            }
            return false;
        });
        return frames.stream().map(entityItemFrame -> entityItemFrame.getPos().relative(entityItemFrame.getDirection().getOpposite())).collect(Collectors.toList());
    }

    private boolean findMeeCreepBoxOnGround() {
        BlockPos position = entity.getEntity().blockPosition();
        List<ItemEntity> items = entity.getWorld().getEntitiesOfClass(ItemEntity.class, worker.getSearchBox(),
                input -> !input.getItem().isEmpty() && input.getItem().getItem() instanceof CreepCubeItem);
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distToCenterSqr(o1.position());
                double d2 = position.distToCenterSqr(o2.position());
                return Double.compare(d1, d2);
            });
            ItemEntity entityItem = items.get(0);
            navigateTo(entityItem, (pos) -> pickup(entityItem));
            return true;
        }
        return false;
    }

    /**
     * See if there is a specific item around. If so start navigating to it and return true
     */
    @Override
    public boolean findItemOnGround(AABB box, Predicate<ItemStack> matcher, Consumer<ItemEntity> job) {
        BlockPos position = entity.blockPosition();
        List<ItemEntity> items = entity.level().getEntitiesOfClass(ItemEntity.class, box, input -> matcher.test(input.getItem()));
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distToCenterSqr(o1.position());
                double d2 = position.distToCenterSqr(o2.position());
                return Double.compare(d1, d2);
            });
            ItemEntity entityItem = items.get(0);
            navigateTo(entityItem, (pos) -> job.accept(entityItem));
            return true;
        }
        return false;
    }

    @Override
    public void putInventoryInChest(BlockPos pos) {
        putInventoryInChestWithMessage(pos, null);
    }

    private void putInventoryInChestWithMessage(BlockPos pos, String message, String... parameters) {
        if (!InventoryTools.isInventory(entity.level(), pos)) {
            // No longer an inventory here. Just drop the items on the ground here
            if (message != null) {
                showMessage("message.meecreeps.inventory_missing");
            }
            entity.dropInventory();
        } else {
            if (message != null) {
                showMessage(message, parameters);
            }
            BlockEntity te = entity.level().getBlockEntity(pos);
            InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
            ChestAnimation.open(entity.level(), pos);
            for (ItemStack stack : entity.getInventory()) {
                if (!stack.isEmpty()) {
                    ItemStack remaining = InventoryTools.insertItemReturnRemaining(handler, stack, false, null);
                    if (!remaining.isEmpty()) {
                        entity.spawnAtLocation((net.minecraft.server.level.ServerLevel) entity.level(), remaining, 0.0f);
                    }
                }
            }
            entity.getInventory().clear();
        }
    }

    private void fetchFromInventory(BlockPos pos, Predicate<ItemStack> matcher, int maxAmount) {
        materialChest = pos;
        Level world = entity.level();
        if (!InventoryTools.isInventory(world, pos)) {
            // No longer an inventory. We cannot get the items from here
            return;
        }
        BlockEntity te = world.getBlockEntity(pos);
        InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
        ChestAnimation.open(world, pos);
        for (int i = 0; i < handler.size(); i++) {
            if (maxAmount <= 0) {
                return;
            }
            ItemStack stack = InventoryTools.getStack(handler, i);
            if (stack == null) {
                // There are still bad mods!
                String badBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
                MeeCreeps.setup.getLogger().warn("Block " + badBlock + " is returning null for handler.getStackInSlot()! That's a bug!");
            } else if (!stack.isEmpty() && matcher.test(stack)) {
                ItemStack extracted = InventoryTools.extract(handler, i, Math.min(maxAmount, stack.getCount()));
                ItemStack remaining = entity.addStack(extracted);
                maxAmount -= extracted.getCount() - remaining.getCount();
                if (!remaining.isEmpty()) {
                    InventoryTools.insertItemReturnRemaining(handler, i, remaining, false, null);
                }
            }
        }
    }

    private float calculateScore(int countMatching, int countFreeForMatching) {
        return 2.0f * countMatching + countFreeForMatching;
    }

    protected boolean findInventoryContainingMost(List<BlockPos> inventoryList, Predicate<ItemStack> matcher, Consumer<BlockPos> job) {
        Level world = entity.level();
        List<BlockPos> inventories = new ArrayList<>();
        Map<BlockPos, Float> countMatching = new HashMap<>();
        for (BlockPos pos : inventoryList) {
            BlockEntity te = world.getBlockEntity(pos);
            InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
            int cnt = 0;
            for (int i = 0; i < handler.size(); i++) {
                ItemStack stack = InventoryTools.getStack(handler, i);
                if (stack == null) {
                    // There are still bad mods!
                    String badBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
                    MeeCreeps.setup.getLogger().warn("Block " + badBlock + " is returning null for handler.getStackInSlot()! That's a bug!");
                } else if (!stack.isEmpty()) {
                    if (matcher.test(stack)) {
                        cnt += stack.getCount();
                    }
                }
            }
            if (cnt > 0) {
                inventories.add(pos);
                countMatching.put(pos, (float) cnt);
            }
        }
        if (inventories.isEmpty()) {
            return false;
        } else {
            // Sort so that highest score goes first
            inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
            navigateTo(inventories.get(0), job);
            return true;
        }
    }

    protected boolean findInventoryContainingMost(AABB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job) {
        Level world = entity.level();
        List<BlockPos> inventories = new ArrayList<>();
        Map<BlockPos, Float> countMatching = new HashMap<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> InventoryTools.isInventory(world, pos),
                (pos, state) -> {
                    BlockEntity te = world.getBlockEntity(pos);
                    InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
                    int cnt = 0;
                    for (int i = 0; i < handler.size(); i++) {
                        ItemStack stack = InventoryTools.getStack(handler, i);
                        if (stack == null) {
                            // There are still bad mods!
                            String badBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
                            MeeCreeps.setup.getLogger().warn("Block " + badBlock + " is returning null for handler.getStackInSlot()! That's a bug!");
                        } else if (!stack.isEmpty()) {
                            if (matcher.test(stack)) {
                                cnt += stack.getCount();
                            }
                        }
                    }
                    if (cnt > 0) {
                        inventories.add(pos);
                        countMatching.put(pos, (float) cnt);
                    }
                });
        if (inventories.isEmpty()) {
            return false;
        } else {
            // Sort so that highest score goes first
            inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
            navigateTo(inventories.get(0), job);
            return true;
        }
    }

    // Default implementation checks materialChest first and otherwise assumes the action was centered on the chest. Override if that's not applicable
    private boolean findChestToPutItemsIn() {
        for (PreferedChest chest : worker.getPreferedChests()) {
            switch (chest) {
                case MARKED:
                    List<BlockPos> meeCreepChests = findMeeCreepChests(worker.getSearchBox());
                    if (!meeCreepChests.isEmpty()) {
                        navigateTo(meeCreepChests.get(0), p -> putInventoryInChestWithMessage(p, "message.meecreeps.put_stuff_away_marked"));
                        return true;
                    }
                    break;
                case TARGET:
                    BlockPos pos = options.getTargetPos();
                    if (InventoryTools.isInventory(entity.level(), pos)) {
                        navigateTo(pos, p -> putInventoryInChestWithMessage(p, "message.meecreeps.put_stuff_away_target"));
                        return true;
                    }
                    break;
                case FIND_MATCHING_INVENTORY:
                    if (findSuitableInventory(worker.getSearchBox(), entity.getInventoryMatcher(),
                            this::putAwayAndTellPlayerTheDistance)) {
                        return true;
                    }
                    break;
                case LAST_CHEST:
                    if (materialChest != null) {
                        if (InventoryTools.isInventory(entity.level(), materialChest)) {
                            navigateTo(materialChest, this::putAwayAndTellPlayerTheDistance);
                            return true;
                        }
                    }
                    break;
            }
        }

        return false;
    }

    private void putAwayAndTellPlayerTheDistance(BlockPos p) {
        ServerPlayer player = getPlayer();
        double dist = 0;
        if (player != null) {
            dist = Math.sqrt(player.blockPosition().distSqr(p));
        }
        putInventoryInChestWithMessage(p, "message.meecreeps.put_stuff_away_specific",
                Integer.toString((int) dist));
    }

    protected boolean needToFindChest(boolean timeToWrapUp) {
        return needsToPutAway || (timeToWrapUp && entity.hasStuffInInventory());
    }

    @Override
    public boolean findSuitableInventory(AABB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job) {
        Level world = entity.level();
        List<BlockPos> inventories = new ArrayList<>();
        Map<BlockPos, Float> countMatching = new HashMap<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> InventoryTools.isInventory(world, pos),
                (pos, state) -> {
                    BlockEntity te = world.getBlockEntity(pos);
                    InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
                    // @todo config?
                    if (handler.size() > 8) {
                        int cnt = 0;
                        int free = 0;
                        for (int i = 0; i < handler.size(); i++) {
                            ItemStack stack = InventoryTools.getStack(handler, i);
                            if (stack == null) {
                                // There are still bad mods!
                                String badBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString();
                                MeeCreeps.setup.getLogger().warn("Block " + badBlock + " is returning null for handler.getStackInSlot()! That's a bug!");
                            } else if (!stack.isEmpty()) {
                                if (matcher.test(stack)) {
                                    cnt += stack.getCount();
                                    free += handler.getCapacityAsInt(i, handler.getResource(i)) - stack.getCount();
                                }
                            } else {
                                free += handler.getCapacityAsInt(i, handler.getResource(i));
                            }
                        }
                        if (cnt >= 0) {
                            inventories.add(pos);
                            countMatching.put(pos, calculateScore(cnt, free));
                        }
                    }
                });
        if (inventories.isEmpty()) {
            return false;
        } else {
            // Sort so that highest score goes first
            inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
            navigateTo(inventories.get(0), job);
            return true;
        }
    }

//    @Override
//    public List<BlockPos> findInventoriesWithMostSpace(AABB box) {
//        Level world = entity.level();
//        List<BlockPos> inventories = new ArrayList<>();
//        Map<BlockPos, Float> countMatching = new HashMap<>();
//        GeneralTools.traverseBox(world, box,
//                (pos, state) -> InventoryTools.isInventory(world, pos),
//                (pos, state) -> {
//                    BlockEntity te = world.getBlockEntity(pos);
//                    InventoryTools.Inventory handler = InventoryTools.getHandler(te.getLevel(), te.getBlockPos());
//                    // @todo config?
//                    if (handler.size() > 8) {
//                        int free = 0;
//                        for (int i = 0 ; i < handler.size() ; i++) {
//                            ItemStack stack = InventoryTools.getStack(handler, i);
//                            if (stack.isEmpty()) {
//                                free += handler.getCapacityAsInt(i, handler.getResource(i));
//                            }
//                        }
//                        inventories.add(pos);
//                        countMatching.put(pos, (float) free);
//                    }
//                });
//        // Sort so that highest score goes first
//        inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
//        return inventories;
//    }

    private boolean tryFindingItemsToPickup() {
        BlockPos position = entity.blockPosition();
        List<ItemEntity> items = itemsToPickup;
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distToCenterSqr(o1.position());
                double d2 = position.distToCenterSqr(o2.position());
                return Double.compare(d1, d2);
            });
            ItemEntity entityItem = items.get(0);
            items.remove(0);
            navigateTo(entityItem, (p) -> pickup(entityItem));
            return true;
        }
        return false;
    }

    /**
     * Return true if the given postion is air, the postion below is not and the postion above is also air
     */
    boolean isStandable(BlockPos pos) {
        Level world = entity.getWorld();
        return !world.isEmptyBlock(pos.below()) && world.isEmptyBlock(pos) && world.isEmptyBlock(pos.above());
    }

    /**
     * Find the nearest suitable spot to stand on at this x,z
     * Or null if there is no suitable position
     */
    private BlockPos findSuitableSpot(BlockPos pos) {
        if (isStandable(pos)) {
            return pos;
        }
        if (isStandable(pos.below())) {
            return pos.below();
        }
        if (isStandable(pos.above())) {
            return pos.above();
        }
        if (isStandable(pos.below(2))) {
            return pos.below(2);
        }
        return null;
    }

    /**
     * Calculate the best spot to move too for reaching the given position
     */
    @Override
    public BlockPos findBestNavigationSpot(BlockPos pos) {
        Entity ent = entity.getEntity();
        Level world = entity.getWorld();

        BlockPos spotN = findSuitableSpot(pos.north());
        BlockPos spotS = findSuitableSpot(pos.south());
        BlockPos spotW = findSuitableSpot(pos.west());
        BlockPos spotE = findSuitableSpot(pos.east());

        double dn = spotN == null ? Double.MAX_VALUE : spotN.distToCenterSqr(ent.getX(), ent.getY(), ent.getZ());
        double ds = spotS == null ? Double.MAX_VALUE : spotS.distToCenterSqr(ent.getX(), ent.getY(), ent.getZ());
        double de = spotE == null ? Double.MAX_VALUE : spotE.distToCenterSqr(ent.getX(), ent.getY(), ent.getZ());
        double dw = spotW == null ? Double.MAX_VALUE : spotW.distToCenterSqr(ent.getX(), ent.getY(), ent.getZ());
        BlockPos p;
        if (dn <= ds && dn <= de && dn <= dw) {
            p = spotN;
        } else if (ds <= de && ds <= dw && ds <= dn) {
            p = spotS;
        } else if (de <= dn && de <= dw && de <= ds) {
            p = spotE;
        } else {
            p = spotW;
        }

        if (p == null) {
            // No suitable spot. Try standing on top
            p = findSuitableSpot(pos);
            // We also need to be able to jump up one spot
            if (p != null && !world.isEmptyBlock(p.above(2))) {
                p = null;
            }
        }

        return p;
    }

    public void readFromNBT(CompoundTag tag) {
        worker.readFromNBT(tag);
        if (tag.contains("materialChest")) {
            materialChest = BlockPos.of(tag.getLongOr("materialChest", 0L));
        }
    }

    public void writeToNBT(CompoundTag tag) {
        worker.writeToNBT(tag);
        if (materialChest != null) {
            tag.putLong("materialChest", materialChest.asLong());
        }
    }
}
