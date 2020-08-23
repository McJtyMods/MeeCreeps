package mcjty.meecreeps.entities;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.network.PacketActionOptionToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.setup.GuiProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class EntityMeeCreeps extends CreatureEntity implements IMeeCreep {
    private static final DataParameter<Optional<BlockState>> CARRIED_BLOCK = EntityDataManager.createKey(EntityMeeCreeps.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<Integer> FACE_VARIATION = EntityDataManager.createKey(EntityMeeCreeps.class, DataSerializers.VARINT);

    public static final ResourceLocation LOOT = new ResourceLocation(MeeCreeps.MODID, "entities/meecreeps");

    public static final int INVENTORY_SIZE = 4;

    private int actionId = 0;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private MeeCreepWorkerTask workerTask;
    private int variationHair = 0;

    // If we are carrying a TE then this contains the NBT data
    private CompoundNBT carriedNBT = null;

    public EntityMeeCreeps(World worldIn) {
        super(worldIn);
        setSize(0.6F, 1.95F);
        variationHair = worldIn.rand.nextInt(9);
        enablePersistence();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.equals(DamageSource.CACTUS)) {
            return true;
        }
        if (source.equals(DamageSource.DROWN)) {
            return true;
        }
        if (source.equals(DamageSource.FALL)) {
            return true;
        }
        if (source.equals(DamageSource.IN_WALL)) {
            return true;
        }
        if (source.equals(DamageSource.CRAMMING)) {
            return true;
        }
        if (source.equals(DamageSource.GENERIC)) {
            return true;
        }

        return super.isInvulnerableTo(source);
    }

    @Override
    public Random getRandom() {
        return getRNG();
    }

    @Override
    public CreatureEntity getEntity() {
        return this;
    }

    public WorkerHelper getHelper() {
        return workerTask.getHelper();
    }

    @Override
    public World getWorld() {
        return getEntityWorld();
    }

    /// Cancel the current job
    public void cancelJob() {
        if (workerTask != null) {
            workerTask.cancelJob();
        }
    }

    @Override
    protected void init() {
        super.init();
        int variationFace = world.rand.nextInt(9);
        // Avoid the engry face
        while (variationFace == 1) {
            variationFace = world.rand.nextInt(9);
        }
        this.dataManager.register(CARRIED_BLOCK, Optional.empty());
        this.dataManager.register(FACE_VARIATION, variationFace);
    }

    public int getVariationFace() {
        return this.dataManager.get(FACE_VARIATION);
    }

    public int getVariationHair() {
        return variationHair;
    }

    public void setVariationFace(int variationFace) {
        this.dataManager.set(FACE_VARIATION, variationFace);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttributes().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(3, workerTask);
        this.goalSelector.addGoal(0, new Watrch(this, 1D, 10));
        this.goalSelector.addGoal(0, new RandomSwimmingGoal(this, 1D, 10));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
//        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
//        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
//        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));

        CreeperEntity
        workerTask = new MeeCreepWorkerTask(this);
        this.tasks.addTask(3, workerTask);
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
//        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMeeCreeps.class, false));
    }


    @Override
    protected boolean processInteract(PlayerEntity player, Hand hand) {
        if (player.getEntityWorld().isRemote) {
//            player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_DISMISS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        } else {
            ServerActionManager manager = ServerActionManager.getManager();
            if (actionId != 0) {
                ActionOptions options = manager.getOptions(actionId);
                if (options != null) {
                    PacketHandler.INSTANCE.sendTo(new PacketActionOptionToClient(options, GuiProxy.GUI_MEECREEP_DISMISS), (EntityPlayerMP) player);
                    options.setPaused(true);
                }
            }
            return true;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isRemote) {
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
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    public void setHeldBlockState(@Nullable BlockState state) {
        this.dataManager.set(CARRIED_BLOCK, Optional.ofNullable(state));
    }

    @Nullable
    public BlockState getHeldBlockState() {
        return this.dataManager.get(CARRIED_BLOCK).orElse(Blocks.AIR.getDefaultState());
    }

    @Nullable
    public CompoundNBT getCarriedNBT() {
        return carriedNBT;
    }

    public void setCarriedNBT(CompoundNBT carriedNBT) {
        this.carriedNBT = carriedNBT;
    }

    public void placeDownBlock(BlockPos pos) {
        // @todo what if this fails?
        BlockState state = getHeldBlockState();
        if (state == null) {
            return;
        }
        if (state.getBlock() == ModBlocks.heldCubeBlock) {
            return;
        }

        world.setBlockState(pos, state, 3);
        CompoundNBT tc = getCarriedNBT();
        if (tc != null) {
            tc.putInt("x", pos.getX());
            tc.putInt("y", pos.getY());
            tc.putInt("z", pos.getZ());
            TileEntity tileEntity = TileEntity.create(tc);
            if (tileEntity != null) {
                world.getChunkAt(pos).addTileEntity(tileEntity);
                tileEntity.markDirty();
                world.notifyBlockUpdate(pos, state, state, 3);
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

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
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
                if (ItemStack.areItemStacksEqual(s, stack)) {
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
                entityDropItem(stack, 0.0f);
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
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        actionId = compound.getInt("actionId");
        ListNBT list = compound.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (i < inventory.size()) {
                inventory.set(i, ItemStack.read(list.getCompound(i)));
            }
        }
        if (compound.contains("worker") && workerTask != null) {
            workerTask.readFromNBT(compound.getCompound("worker"));
        }

        BlockState BlockState = null;
        if (compound.contains("carried")) {
            BlockState = NBTUtil.readBlockState(compound.getCompound("carried"));
        }

        if (BlockState == null || BlockState.getMaterial() == Material.AIR) {
            BlockState = null;
        }

        this.setHeldBlockState(BlockState);
        if (compound.contains("carriedNBT")) {
            carriedNBT = compound.getCompound("carriedNBT");
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.putInt("actionId", actionId);

        ListNBT list = new ListNBT();
        for (ItemStack stack : inventory) {
            list.add(stack.write(new CompoundNBT()));
        }

        compound.put("items", list);
        if (workerTask != null) {
            CompoundNBT workerTag = new CompoundNBT();
            workerTask.writeToNBT(workerTag);
            compound.put("worker", workerTag);
        }

        BlockState BlockState = this.getHeldBlockState();
        if (BlockState != null) {
            compound.put("carried", NBTUtil.writeBlockState(BlockState));
        }

        if (carriedNBT != null) {
            compound.put("carriedNBT", carriedNBT);
        }

        return compound;
    }

    private void spawnDeathParticles() {
        for (int i = 0; i < 40; i++) {
            world.addParticle(ParticleTypes.CLOUD, getPosX(), getPosY() + .5, getPosZ(), (rand.nextDouble() - .5) * .2, (rand.nextDouble() - .5) * .5, (rand.nextDouble() - .5) * .2);
        }
        world.playSound(getPosX(), getPosY(), getPosZ(), SoundEvents.ENTITY_CREEPER_HURT, this.getSoundCategory(), 1.0F, 1.0F, false);
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public void remove(boolean keepData) {
        super.remove(keepData);

        // todo: verify this works
        spawnDeathParticles();
    }

    private void killMe() {
        remove();
        dropInventory();
        placeDownBlock(getPosition());
    }

    @Override
    public void onDeath(@SuppressWarnings("NullableProblems") DamageSource cause) {
        super.onDeath(cause);
        killMe();
    }
}
