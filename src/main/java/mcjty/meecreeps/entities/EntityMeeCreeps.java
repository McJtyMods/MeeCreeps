package mcjty.meecreeps.entities;

import mcjty.meecreeps.MeeCreeps;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.ServerActionManager;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class EntityMeeCreeps extends EntityCreature {

    public static final ResourceLocation LOOT = new ResourceLocation(MeeCreeps.MODID, "entities/meecreeps");

    public static final int INVENTORY_SIZE = 3;

    private int actionId = 0;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public EntityMeeCreeps(World worldIn) {
        super(worldIn);
        setSize(0.6F, 1.95F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // Here we set various attributes for our mob. Like maximum health, armor, speed, ...
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.13D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
//        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
//        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(3, new MeeCreepWorkerTask(this));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    private void applyEntityAI() {
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        ServerActionManager manager = ServerActionManager.getManager();
        if (actionId != 0) {
            ActionOptions options = manager.getOptions(actionId);
            if (options == null) {
                // We should die @todo animation
                this.setDead();
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LOOT;
    }

    // Add an itemstack to the internal inventory and return what could not be added
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


    public NonNullList<ItemStack> getInventory() {
        return inventory;
    }

    public boolean isEmptyInventory() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void dropInventory() {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                entityDropItem(stack, 0.0f);
            }
            inventory.set(i, ItemStack.EMPTY);
        }
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
    }

    private void spawnDeathParticles() {
        for (int i = 0 ; i < 40 ; i++) {
            world.spawnParticle(EnumParticleTypes.CLOUD, posX, posY+.5, posZ, (rand.nextDouble()-.5)*.2, (rand.nextDouble()-.5)*.5, (rand.nextDouble()-.5)*.2);
        }
        world.playSound(posX, posY, posZ, SoundEvents.ENTITY_CREEPER_HURT, this.getSoundCategory(), 1.0F, 1.0F, false);
    }

    @Override
    public void setDead() {
        super.setDead();
        dropInventory();
        spawnDeathParticles();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        dropInventory();
        spawnDeathParticles();
    }
}
