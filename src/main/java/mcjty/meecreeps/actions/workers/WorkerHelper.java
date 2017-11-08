package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.ForgeEventHandlers;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.PacketShowBalloonToClient;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.api.*;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.network.PacketHandler;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.InventoryTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WorkerHelper implements IWorkerHelper {

    private final double DISTANCE_TOLERANCE = 1.4;

    private IActionWorker worker;
    protected final ActionOptions options;
    protected final EntityMeeCreeps entity;
    protected boolean needsToPutAway = false;
    protected int waitABit = 10;
    private int speed = 10;

    protected BlockPos movingToPos;
    protected Entity movingToEntity;
    private int pathTries = 0;
    protected Consumer<BlockPos> job;
    protected List<EntityItem> itemsToPickup = new ArrayList<>();
    private BlockPos materialChest;

    private boolean messageShown = false;

    public WorkerHelper(EntityMeeCreeps entity, IActionContext options) {
        this.options = (ActionOptions) options;
        this.entity = entity;
    }

    public void setWorker(IActionWorker worker) {
        this.worker = worker;
    }

    public IActionWorker getWorker() {
        return worker;
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

    @Override
    public void giveDropsToMeeCreeps(@Nonnull List<ItemStack> drops) {
        for (ItemStack stack : drops) {
            ItemStack remaining = entity.addStack(stack);
            if (!remaining.isEmpty()) {
                itemsToPickup.add(entity.entityDropItem(remaining, 0.0f));
                needsToPutAway = true;
            }
        }
    }

    protected void showMessage(String message) {
        if (!messageShown) {
            messageShown = true;
            EntityPlayerMP player = getPlayer();
            if (player != null) {
                PacketHandler.INSTANCE.sendTo(new PacketShowBalloonToClient(message), player);
            }
        }
    }

    protected void clearMessage() {
        messageShown = false;
    }

    @Override
    public void registerHarvestableBlock(BlockPos pos) {
        ForgeEventHandlers.harvestableBlocksToCollect.put(pos, options.getActionId());
    }

    @Override
    public void navigateTo(BlockPos pos, Consumer<BlockPos> job) {
        BlockPos position = entity.getPosition();
        double d = Math.min(position.distanceSq(pos), position.add(0, entity.getEyeHeight(), 0).distanceSq(pos));
        if (d < DISTANCE_TOLERANCE) {
            job.accept(pos);
        } else if (!entity.getNavigator().tryMoveToXYZ(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 2.0)) {
            // We need to teleport
            entity.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
            job.accept(pos);
        } else {
            this.movingToPos = pos;
            this.movingToEntity = null;
            pathTries = 1;
            this.job = job;
        }
    }

    @Override
    public boolean navigateTo(Entity dest, Consumer<BlockPos> job, double maxDist) {
        if (dest == null || dest.isDead) {
            return false;
        }
        BlockPos position = entity.getPosition();
        double d = Math.min(position.distanceSq(dest.posX, dest.posY, dest.posZ), position.add(0, entity.getEyeHeight(), 0).distanceSq(dest.posX, dest.posY, dest.posZ));
        if (d > maxDist*maxDist) {
            return false;
        } else if (d < DISTANCE_TOLERANCE) {
            job.accept(dest.getPosition());
        } else if (!entity.getNavigator().tryMoveToEntityLiving(dest, 2.0)) {
            // We need to teleport
            entity.setPositionAndUpdate(dest.posX, dest.posY, dest.posZ);
            job.accept(dest.getPosition());
        } else {
            this.movingToPos = null;
            this.movingToEntity = dest;
            pathTries = 1;
            this.job = job;
        }
        return true;
    }

    @Override
    public boolean navigateTo(Entity dest, Consumer<BlockPos> job) {
        return navigateTo(dest, job, 1000000000);
    }

    public void tick(boolean timeToWrapUp) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = speed;

        if (job != null) {
            BlockPos position = entity.getPosition();
            if (movingToEntity != null) {
                if (movingToEntity.isDead) {
                    job = null;
                } else {
                    double d = position.distanceSq(movingToEntity.posX, movingToEntity.posY, movingToEntity.posZ);
                    if (d < DISTANCE_TOLERANCE) {
                        job.accept(movingToEntity.getPosition());
                        job = null;
                    } else if (entity.getNavigator().noPath()) {
                        if (pathTries > 2) {
                            entity.setPositionAndUpdate(movingToEntity.posX, movingToEntity.posY, movingToEntity.posZ);
                            job.accept(movingToEntity.getPosition());
                            job = null;
                        } else {
                            pathTries++;
                            entity.getNavigator().tryMoveToEntityLiving(movingToEntity, 2.0);
                        }
                    }
                }
            } else {
                double d = position.distanceSq(movingToPos.getX(), movingToPos.getY(), movingToPos.getZ());
                if (d < DISTANCE_TOLERANCE) {
                    job.accept(movingToPos);
                    job = null;
                } else if (entity.getNavigator().noPath()) {
                    if (pathTries > 2) {
                        entity.setPositionAndUpdate(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5);
                        job.accept(movingToPos);
                        job = null;
                    } else {
                        pathTries++;
                        entity.getNavigator().tryMoveToXYZ(movingToPos.getX() + .5, movingToPos.getY(), movingToPos.getZ() + .5, 2.0);
                    }
                }
            }
        } else if (!options.getDrops().isEmpty()) {
            // There are drops we need to collect first.
            for (Pair<BlockPos, ItemStack> pair : options.getDrops()) {
                ItemStack drop = pair.getValue();
                if (!drop.isEmpty()) {
                    ItemStack remaining = entity.addStack(drop);
                    if (!remaining.isEmpty()) {
                        entity.entityDropItem(remaining, 0.0f);
                        needsToPutAway = true;
                    }
                }
            }
            options.clearDrops();
            ServerActionManager.getManager().save();
            waitABit = 1;   // Process faster
        } else if (needToFindChest(timeToWrapUp)) {
            if (!findChestToPutItemsIn()) {
                if (!navigateTo(getPlayer(), (p) -> giveToPlayerOrDrop(), 12)) {
                    entity.dropInventory();
                }
            }
            needsToPutAway = false;
        } else if (!itemsToPickup.isEmpty()) {
            tryFindingItemsToPickup();
        } else {
            worker.tick(timeToWrapUp);
        }
    }

    @Override
    public void harvestAndPickup(BlockPos pos) {
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester());
        SoundTools.playSound(world, block.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        entity.getEntityWorld().setBlockToAir(pos);
        giveDropsToMeeCreeps(drops);
    }


    @Override
    public void harvestAndDrop(BlockPos pos) {
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester());
        SoundTools.playSound(world, block.getSoundType().getBreakSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
        entity.getEntityWorld().setBlockToAir(pos);
        for (ItemStack stack : drops) {
            entity.entityDropItem(stack, 0.0f);
        }
    }


    @Override
    public void pickup(EntityItem item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }

    @Override
    public boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, entityPlayer)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
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
        EntityItem entityItem = entity.getEntity().entityDropItem(stack, 0.0f);
        itemsToPickup.add(entityItem);
        putStuffAway();
    }

    @Override
    public void giveToPlayerOrDrop() {
        EntityPlayerMP player = getPlayer();
        BlockPos position = entity.getPosition();
        if (player == null || position.distanceSq(player.getPosition()) > 2 * 2) {
            entity.dropInventory();
        } else {
            List<ItemStack> remaining = new ArrayList<>();
            for (ItemStack stack : entity.getInventory()) {
                if (!player.inventory.addItemStackToInventory(stack)) {
                    remaining.add(stack);
                }
            }
            player.openContainer.detectAndSendChanges();
            for (ItemStack stack : remaining) {
                entity.entityDropItem(stack, 0.0f);
            }
            entity.getInventory().clear();
        }

    }

    @Nullable
    protected EntityPlayerMP getPlayer() {
        return (EntityPlayerMP) options.getPlayer();
    }

    @Override
    public void findItemOnGroundOrInChest(Predicate<ItemStack> matcher, String message) {
        if (!findItemOnGround(worker.getActionBox(), matcher, this::pickup)) {
            if (!findInventoryContainingMost(worker.getActionBox(), matcher, p -> fetchFromInventory(p, matcher))) {
                showMessage(message);
            } else {
                clearMessage();
            }
        } else {
            clearMessage();
        }
    }

    /**
     * See if there is a specific item around. If so start navigating to it and return true
     */
    @Override
    public boolean findItemOnGround(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<EntityItem> job) {
        BlockPos position = entity.getPosition();
        List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box, input -> matcher.test(input.getItem()));
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            navigateTo(entityItem, (pos) -> job.accept(entityItem));
            return true;
        }
        return false;
    }

    @Override
    public void putInventoryInChest(BlockPos pos) {
        TileEntity te = entity.getEntityWorld().getTileEntity(pos);
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        for (ItemStack stack : entity.getInventory()) {
            if (!stack.isEmpty()) {
                ItemStack remaining = ItemHandlerHelper.insertItem(handler, stack, false);
                if (!remaining.isEmpty()) {
                    entity.entityDropItem(remaining, 0.0f);
                }
            }
        }
        entity.getInventory().clear();
    }

    protected void fetchFromInventory(BlockPos pos, Predicate<ItemStack> matcher) {
        materialChest = pos;
        TileEntity te = entity.getEntityWorld().getTileEntity(pos);
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        for (int i = 0 ; i < handler.getSlots() ; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && matcher.test(stack)) {
                ItemStack extracted = handler.extractItem(i, stack.getCount(), false);
                ItemStack remaining = entity.addStack(extracted);
                if (!remaining.isEmpty()) {
                    handler.insertItem(i, remaining, false);
                }
            }
        }
    }

    private float calculateScore(int countMatching, int countFreeForMatching) {
        return 2.0f * countMatching + countFreeForMatching;
    }

    protected boolean findInventoryContainingMost(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job) {
        World world = entity.getEntityWorld();
        List<BlockPos> inventories = new ArrayList<>();
        Map<BlockPos, Float> countMatching = new HashMap<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> InventoryTools.isInventory(world, pos),
                (pos, state) -> {
                    TileEntity te = world.getTileEntity(pos);
                    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                    int cnt  = 0;
                    for (int i = 0 ; i < handler.getSlots() ; i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty()) {
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
    protected boolean findChestToPutItemsIn() {
        for (PreferedChest chest : worker.getPreferedChests()) {
            switch (chest) {
                case TARGET:
                    BlockPos pos = options.getTargetPos();
                    if (InventoryTools.isInventory(entity.getEntityWorld(), pos)) {
                        navigateTo(pos, this::putInventoryInChest);
                        return true;
                    }
                    break;
                case FIND_MATCHING_INVENTORY:
                    if (findSuitableInventory(worker.getActionBox(), entity.getInventoryMatcher(), this::putInventoryInChest)) {
                        return true;
                    }
                    break;
                case LAST_CHEST:
                    if (materialChest != null) {
                        if (InventoryTools.isInventory(entity.getEntityWorld(), materialChest)) {
                            navigateTo(materialChest, this::putInventoryInChest);
                            return true;
                        }
                    }
                    break;
            }
        }

        return false;
    }

    protected boolean needToFindChest(boolean timeToWrapUp) {
        return needsToPutAway || (timeToWrapUp && entity.hasStuffInInventory());
    }

    @Override
    public boolean findSuitableInventory(AxisAlignedBB box, Predicate<ItemStack> matcher, Consumer<BlockPos> job) {
        World world = entity.getEntityWorld();
        List<BlockPos> inventories = new ArrayList<>();
        Map<BlockPos, Float> countMatching = new HashMap<>();
        GeneralTools.traverseBox(world, box,
                (pos, state) -> InventoryTools.isInventory(world, pos),
                (pos, state) -> {
                    TileEntity te = world.getTileEntity(pos);
                    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                    // @todo config?
                    if (handler.getSlots() > 8) {
                        int cnt = 0;
                        int free = 0;
                        for (int i = 0 ; i < handler.getSlots() ; i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                if (matcher.test(stack)) {
                                    cnt += stack.getCount();
                                    free += handler.getSlotLimit(i) - stack.getCount();
                                }
                            } else {
                                free += handler.getSlotLimit(i);
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
//    public List<BlockPos> findInventoriesWithMostSpace(AxisAlignedBB box) {
//        World world = entity.getEntityWorld();
//        List<BlockPos> inventories = new ArrayList<>();
//        Map<BlockPos, Float> countMatching = new HashMap<>();
//        GeneralTools.traverseBox(world, box,
//                (pos, state) -> InventoryTools.isInventory(world, pos),
//                (pos, state) -> {
//                    TileEntity te = world.getTileEntity(pos);
//                    IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
//                    // @todo config?
//                    if (handler.getSlots() > 8) {
//                        int free = 0;
//                        for (int i = 0 ; i < handler.getSlots() ; i++) {
//                            ItemStack stack = handler.getStackInSlot(i);
//                            if (stack.isEmpty()) {
//                                free += handler.getSlotLimit(i);
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
        BlockPos position = entity.getPosition();
        List<EntityItem> items = itemsToPickup;
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            items.remove(0);
            navigateTo(entityItem, (p) -> pickup(entityItem));
            return true;
        }
        return false;
    }

    public void readFromNBT(NBTTagCompound tag) {
        worker.readFromNBT(tag);
        if (tag.hasKey("materialChest")) {
            materialChest = BlockPos.fromLong(tag.getLong("materialChest"));
        }
    }

    public void writeToNBT(NBTTagCompound tag) {
        worker.writeToNBT(tag);
        if (materialChest != null) {
            tag.setLong("materialChest", materialChest.toLong());
        }
    }
}
