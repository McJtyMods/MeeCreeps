package mcjty.meecreeps.entities;

import mcjty.meecreeps.setup.Registration;

import java.util.Optional;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.PacketActionOptionToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.GuiProxy;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class EntityMeeCreeps extends PathfinderMob implements IMeeCreep {

    private static final EntityDataAccessor<Optional<BlockState>> CARRIED_BLOCK = SynchedEntityData.<Optional<BlockState>>defineId(EntityMeeCreeps.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);
    private static final EntityDataAccessor<Integer> FACE_VARIATION = SynchedEntityData.<Integer>defineId(EntityMeeCreeps.class, EntityDataSerializers.INT);

    public static final ResourceLocation LOOT = ResourceLocation.fromNamespaceAndPath(MeeCreeps.MODID, "entities/meecreeps");

    public static final int INVENTORY_SIZE = 4;

    private int actionId = 0;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private MeeCreepWorkerTask workerTask;
    private int variationHair = 0;

    // If we are carrying a TE then this contains the NBT data
    private CompoundTag carriedNBT = null;

    public EntityMeeCreeps(net.minecraft.world.entity.EntityType<? extends EntityMeeCreeps> type, Level worldIn) {
        super(type, worldIn);
        variationHair = worldIn.random.nextInt(9);
        setPersistenceRequired();
    }

    public EntityMeeCreeps(Level world) {
        this(Registration.CREEP.get(), world);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.CACTUS)
                || source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)
                || source.is(net.minecraft.world.damagesource.DamageTypes.FALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || source.is(net.minecraft.world.damagesource.DamageTypes.CRAMMING)
                || super.isInvulnerableTo(source);
    }

    @Override
    public PathfinderMob getEntity() {
        return this;
    }

    public WorkerHelper getHelper() {
        return workerTask == null ? null : workerTask.getHelper();
    }

    @Override
    public Level getWorld() {
        return level();
    }

    /// Cancel the current job
    public void cancelJob() {
        if (workerTask != null) {
            workerTask.cancelJob();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        int variationFace = level().random.nextInt(9);
        // Avoid the engry face
        while (variationFace == 1) {
            variationFace = level().random.nextInt(9);
        }
        builder.define(CARRIED_BLOCK, Optional.empty());
        builder.define(FACE_VARIATION, variationFace);
    }

    public int getVariationFace() {
        return this.entityData.get(FACE_VARIATION);
    }

    public int getVariationHair() {
        return variationHair;
    }

    public void setVariationFace(int variationFace) {
        this.entityData.set(FACE_VARIATION, variationFace);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return net.minecraft.world.entity.Mob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 35).add(Attributes.MOVEMENT_SPEED, .13)
                .add(Attributes.ARMOR, 2).add(Attributes.ATTACK_DAMAGE, 2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
//        this.goalSelector.addGoal(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
//        this.goalSelector.addGoal(7, new EntityAIWander(this, 1.0D));
//        this.goalSelector.addGoal(2, new EntityAIAttackMelee(this, 1.0D, true));

        workerTask = new MeeCreepWorkerTask(this);
        this.goalSelector.addGoal(3, workerTask);
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
//        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMeeCreeps.class, false));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            ServerActionManager manager = ServerActionManager.getManager();
            if (actionId != 0) {
                ActionOptions options = manager.getOptions(actionId);
                if (options != null && java.util.Objects.equals(options.getPlayerId(), player.getUUID())) {
                    MeeCreepsMessages.INSTANCE.sendTo(new PacketActionOptionToClient(options, GuiProxy.GUI_MEECREEP_DISMISS), (ServerPlayer) player);
                    options.setPaused(true);
                }
            }
            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            ServerActionManager manager = ServerActionManager.getManager();
            if (actionId != 0) {
                ActionOptions options = manager.getOptions(actionId);
                if (options == null) {
                    manager.updateEntityCache(actionId, null);
                    this.killMe();
                } else {
                    manager.updateEntityCache(actionId, this);
                }
            }
        }
    }

    @Override
    @Nullable
    protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, LOOT);
    }

    public void setHeldBlockState(@Nullable BlockState state) {
        this.entityData.set(CARRIED_BLOCK, Optional.ofNullable(state));
    }

    @Nullable
    public BlockState getHeldBlockState() {
        return this.entityData.get(CARRIED_BLOCK).orElse(null);
    }

    @Nullable
    public CompoundTag getCarriedNBT() {
        return carriedNBT;
    }

    public void setCarriedNBT(CompoundTag carriedNBT) {
        this.carriedNBT = carriedNBT;
    }

    public void placeDownBlock(BlockPos pos) {
        // @todo what if this fails?
        BlockState state = getHeldBlockState();
        if (state == null) {
            return;
        }
        if (state.getBlock() == Registration.CUBE.get()) {
            return;
        }

        if (!level().getBlockState(pos).canBeReplaced() || !level().setBlock(pos, state, 3))
            return;
        CompoundTag tc = getCarriedNBT();
        if (tc != null) {
            tc.putInt("x", pos.getX());
            tc.putInt("y", pos.getY());
            tc.putInt("z", pos.getZ());
            BlockEntity tileEntity = level().getBlockEntity(pos);
            if (tileEntity != null) {
                tileEntity.loadWithComponents(tc, level().registryAccess());
                tileEntity.setChanged();
                level().sendBlockUpdated(pos, state, state, 3);
            }
        }

        carriedNBT = null;
        setHeldBlockState(null);
    }

    // Add an itemstack to the internal inventory and return what could not be added
    @Override
    public ItemStack addStack(ItemStack stack) {
        int i = 0;

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (i >= INVENTORY_SIZE) {
                    break;
                }

                ItemStack itemstack = this.inventory.get(i);

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && ItemStack.isSameItemSameComponents(stack, itemstack)) {
                    int newsize = itemstack.getCount() + stack.getCount();
                    int maxSize = itemstack.getMaxStackSize();

                    if (newsize <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(newsize);
                        return ItemStack.EMPTY;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                    }
                }

                ++i;
            }
        }

        if (!stack.isEmpty()) {
            for (i = 0; i < INVENTORY_SIZE; i++) {
                ItemStack itemstack = this.inventory.get(i);
                if (itemstack.isEmpty()) {
                    this.inventory.set(i, stack);
                    return ItemStack.EMPTY;
                }
            }
        }

        return stack;
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    @Override
    public Predicate<ItemStack> getInventoryMatcher() {
        return stack -> {
            for (ItemStack s : inventory) {
                if (ItemStack.matches(s, stack)) {
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public boolean hasEmptyInventory() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasStuffInInventory() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasItem(Predicate<ItemStack> matcher) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && matcher.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasItems(Predicate<ItemStack> matcher, int amount) {
        int cnt = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && matcher.test(stack)) {
                cnt += stack.getCount();
                if (cnt >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasRoom(Predicate<ItemStack> matcher) {
        for (ItemStack stack : inventory) {
            if (stack.isEmpty()) {
                return true;
            }
            if (!stack.isEmpty() && matcher.test(stack)) {
                if (stack.getCount() < stack.getMaxStackSize()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void dropInventory() {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack, 0.0f);
            }
            inventory.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack consumeItem(Predicate<ItemStack> matcher, int amount) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && matcher.test(stack)) {
                return stack.split(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 5;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        actionId = compound.getInt("actionId");
        ListTag list = compound.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (i < inventory.size()) {
                inventory.set(i, ItemStack.parseOptional(level().registryAccess(), list.getCompound(i)));
            }
        }
        if (compound.contains("worker") && workerTask != null) {
            workerTask.readFromNBT(compound.getCompound("worker"));
        }

        setHeldBlockState(compound.contains("carried", Tag.TAG_COMPOUND)
                ? net.minecraft.nbt.NbtUtils.readBlockState(level().holderLookup(net.minecraft.core.registries.Registries.BLOCK), compound.getCompound("carried")) : null);
        variationHair = compound.getInt("hair");
        setVariationFace(compound.getInt("face"));
        if (compound.contains("carriedNBT")) {
            carriedNBT = compound.getCompound("carriedNBT");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("actionId", actionId);
        compound.putInt("hair", variationHair);
        compound.putInt("face", getVariationFace());
        ListTag list = new ListTag();
        for (ItemStack stack : inventory) {
            list.add(stack.saveOptional(level().registryAccess()));
        }
        compound.put("items", list);
        if (workerTask != null) {
            CompoundTag workerTag = new CompoundTag();
            workerTask.writeToNBT(workerTag);
            compound.put("worker", workerTag);
        }

        BlockState iblockstate = this.getHeldBlockState();
        if (iblockstate != null) {
            compound.put("carried", net.minecraft.nbt.NbtUtils.writeBlockState(iblockstate));
        }

        if (carriedNBT != null) {
            compound.put("carriedNBT", carriedNBT);
        }
    }

    private void spawnDeathParticles() {
        if (level() instanceof net.minecraft.server.level.ServerLevel server) {
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, getX(), getY() + .5, getZ(), 40, .2, .5, .2, .05);
            level().playSound(null, blockPosition(), SoundEvents.CREEPER_HURT, getSoundSource(), 1, 1);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public void killMe() {
        if (isRemoved())
            return;
        dropInventory();
        placeDownBlock(blockPosition());
        BlockState carried = getHeldBlockState();
        if (carried != null && carried.getBlock() != Registration.CUBE.get()) {
            ItemStack drop = new ItemStack(carried.getBlock());
            if (!drop.isEmpty()) {
                if (carriedNBT != null)
                    drop.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(carriedNBT));
                var properties = net.minecraft.nbt.NbtUtils.writeBlockState(carried).getCompound("Properties");
                if (!properties.isEmpty())
                    drop.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                            new net.minecraft.world.item.component.BlockItemStateProperties(properties.getAllKeys().stream()
                                    .collect(java.util.stream.Collectors.toMap(key -> key, properties::getString))));
                spawnAtLocation(drop);
            }
            carriedNBT = null;
            setHeldBlockState(null);
        }
        spawnDeathParticles();
        discard();
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        killMe();
    }
}
