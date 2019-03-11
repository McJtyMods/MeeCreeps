package mcjty.meecreeps.entities;

import com.google.common.base.Optional;
import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.PacketActionOptionToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IMeeCreep;
import mcjty.meecreeps.blocks.ModBlocks;
import mcjty.meecreeps.network.MeeCreepsMessages;
import mcjty.meecreeps.setup.GuiProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;

public class EntityMeeCreeps extends EntityCreature implements IMeeCreep {

    private static final DataParameter<Optional<IBlockState>> CARRIED_BLOCK = EntityDataManager.<Optional<IBlockState>>createKey(EntityMeeCreeps.class, DataSerializers.OPTIONAL_BLOCK_STATE);
    private static final DataParameter<Integer> FACE_VARIATION = EntityDataManager.<Integer>createKey(EntityMeeCreeps.class, DataSerializers.VARINT);

    public static final ResourceLocation LOOT = new ResourceLocation(MeeCreeps.MODID, "entities/meecreeps");

    public static final int INVENTORY_SIZE = 4;

    private int actionId = 0;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private MeeCreepWorkerTask workerTask;
    private int variationHair = 0;

    // If we are carrying a TE then this contains the NBT data
    private NBTTagCompound carriedNBT = null;

    public EntityMeeCreeps(World worldIn) {
        super(worldIn);
        setSize(0.6F, 1.95F);
        variationHair = worldIn.rand.nextInt(9);
        enablePersistence();
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
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
        return super.isEntityInvulnerable(source);
    }

    @Override
    public Random getRandom() {
        return getRNG();
    }

    @Override
    public EntityCreature getEntity() {
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
    protected void entityInit() {
        super.entityInit();
        int variationFace = world.rand.nextInt(9);
        // Avoid the engry face
        while (variationFace == 1) {
            variationFace = world.rand.nextInt(9);
        }
        this.dataManager.register(CARRIED_BLOCK, Optional.absent());
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
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // Here we set various attributes for our mob. Like maximum health, armor, speed, ...
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
//        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
//        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
//        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));

        workerTask = new MeeCreepWorkerTask(this);
        this.tasks.addTask(3, workerTask);
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
//        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityMeeCreeps.class, false));
    }


    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.getEntityWorld().isRemote) {
//            player.openGui(MeeCreeps.instance, GuiProxy.GUI_MEECREEP_DISMISS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        } else {
            ServerActionManager manager = ServerActionManager.getManager();
            if (actionId != 0) {
                ActionOptions options = manager.getOptions(actionId);
                if (options != null) {
                    MeeCreepsMessages.INSTANCE.sendTo(new PacketActionOptionToClient(options, GuiProxy.GUI_MEECREEP_DISMISS), (EntityPlayerMP) player);
                    options.setPaused(true);
                }
            }
            return true;
        }
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
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

    public void setHeldBlockState(@Nullable IBlockState state) {
        this.dataManager.set(CARRIED_BLOCK, Optional.fromNullable(state));
    }

    @Nullable
    public IBlockState getHeldBlockState() {
        return (IBlockState) ((Optional) this.dataManager.get(CARRIED_BLOCK)).orNull();
    }

    @Nullable
    public NBTTagCompound getCarriedNBT() {
        return carriedNBT;
    }

    public void setCarriedNBT(NBTTagCompound carriedNBT) {
        this.carriedNBT = carriedNBT;
    }

    public void placeDownBlock(BlockPos pos) {
        // @todo what if this fails?
        IBlockState state = getHeldBlockState();
        if (state == null) {
            return;
        }
        if (state.getBlock() == ModBlocks.heldCubeBlock) {
            return;
        }

        world.setBlockState(pos, state, 3);
        NBTTagCompound tc = getCarriedNBT();
        if (tc != null) {
            tc.setInteger("x", pos.getX());
            tc.setInteger("y", pos.getY());
            tc.setInteger("z", pos.getZ());
            TileEntity tileEntity = TileEntity.create(world, tc);
            if (tileEntity != null) {
                world.getChunkFromBlockCoords(pos).addTileEntity(tileEntity);
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

                if (!itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
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
                return stack.splitStack(amount);
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
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        actionId = compound.getInteger("actionId");
        NBTTagList list = compound.getTagList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            if (i < inventory.size()) {
                inventory.set(i, new ItemStack(list.getCompoundTagAt(i)));
            }
        }
        if (compound.hasKey("worker") && workerTask != null) {
            workerTask.readFromNBT(compound.getCompoundTag("worker"));
        }

        IBlockState iblockstate;

        if (compound.hasKey("carried", 8)) {
            iblockstate = Block.getBlockFromName(compound.getString("carried")).getStateFromMeta(compound.getShort("carriedData") & 65535);
        } else {
            iblockstate = Block.getBlockById(compound.getShort("carried")).getStateFromMeta(compound.getShort("carriedData") & 65535);
        }

        if (iblockstate == null || iblockstate.getBlock() == null || iblockstate.getMaterial() == Material.AIR) {
            iblockstate = null;
        }
        this.setHeldBlockState(iblockstate);

        if (compound.hasKey("carriedNBT")) {
            carriedNBT = compound.getCompoundTag("carriedNBT");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("actionId", actionId);
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inventory) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("items", list);
        if (workerTask != null) {
            NBTTagCompound workerTag = new NBTTagCompound();
            workerTask.writeToNBT(workerTag);
            compound.setTag("worker", workerTag);
        }

        IBlockState iblockstate = this.getHeldBlockState();
        if (iblockstate != null) {
            compound.setShort("carried", (short) Block.getIdFromBlock(iblockstate.getBlock()));
            compound.setShort("carriedData", (short) iblockstate.getBlock().getMetaFromState(iblockstate));
        }

        if (carriedNBT != null) {
            compound.setTag("carriedNBT", carriedNBT);
        }
    }

    private void spawnDeathParticles() {
        for (int i = 0; i < 40; i++) {
            world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY + .5, posZ, (rand.nextDouble() - .5) * .2, (rand.nextDouble() - .5) * .5, (rand.nextDouble() - .5) * .2);
        }
        world.playSound(posX, posY, posZ, SoundEvents.ENTITY_CREEPER_HURT, this.getSoundCategory(), 1.0F, 1.0F, false);
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public void setDead() {
        super.setDead();
        spawnDeathParticles();
    }

    private void killMe() {
        setDead();
        dropInventory();
        placeDownBlock(getPosition());
    }

    @Override
    public void onDeath(@SuppressWarnings("NullableProblems") DamageSource cause) {
        super.onDeath(cause);
        killMe();
    }
}
