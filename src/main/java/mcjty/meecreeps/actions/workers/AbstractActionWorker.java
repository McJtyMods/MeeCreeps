package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.InventoryTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractActionWorker implements IActionWorker {

    protected final ActionOptions options;
    protected final EntityMeeCreeps entity;
    protected boolean needsToPutAway = false;
    protected int waitABit = 10;

    protected BlockPos movingToPos;
    protected Entity movingToEntity;
    private int pathTries = 0;
    protected Consumer<BlockPos> job;

    public AbstractActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        this.options = options;
        this.entity = entity;
    }

    protected void navigateTo(BlockPos pos, Consumer<BlockPos> job) {
        BlockPos position = entity.getPosition();
        double d = position.distanceSq(pos.getX(), pos.getY(), pos.getZ());
        if (d < 2) {
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

    protected void navigateTo(Entity dest, Consumer<BlockPos> job) {
        BlockPos position = entity.getPosition();
        double d = position.distanceSq(dest.posX, dest.posY, dest.posZ);
        if (d < 2) {
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
    }

    @Override
    public void tick(boolean lastTask) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = 10;

        if (job != null) {
            BlockPos position = entity.getPosition();
            if (movingToEntity != null) {
                if (movingToEntity.isDead) {
                    job = null;
                } else {
                    double d = position.distanceSq(movingToEntity.posX, movingToEntity.posY, movingToEntity.posZ);
                    if (d < 2) {
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
                if (d < 2) {
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
        } else {
            performTick(lastTask);
        }
    }

    protected void pickup(EntityItem item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }

    protected static boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, entityPlayer)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    protected abstract void performTick(boolean lastTask);

    protected void done() {
        options.setStage(Stage.DONE);
        ServerActionManager.getManager().save();
    }

    // Indicate the task is done and that it is time to do the last task (putting back stuff etc)
    protected void taskIsDone() {
        options.setStage(Stage.TIME_IS_UP);
        ServerActionManager.getManager().save();
    }

    private void giveToPlayerOrDrop() {
        World world = entity.getEntityWorld();
        MinecraftServer server = world.getMinecraftServer();
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(options.getPlayerId());
        BlockPos position = entity.getPosition();
        if (player == null || position.distanceSq(player.getPosition()) > 2*2) {
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

    protected void giveToPlayerIfPossible() {
        World world = entity.getEntityWorld();
        MinecraftServer server = world.getMinecraftServer();
        EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(options.getPlayerId());
        BlockPos position = entity.getPosition();
        if (player == null || position.distanceSq(player.getPosition()) > 12*12) {
            entity.dropInventory();
        } else {
            navigateTo(player, (pos) -> giveToPlayerOrDrop());
        }
    }

    /**
     * See if there is a specific item around. If so start navigating to it and return true
     */
    protected boolean tryFindingSomeItem(AxisAlignedBB box, Predicate<ItemStack> matcher) {
        BlockPos position = entity.getPosition();
        List<EntityItem> items = entity.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box, input -> matcher.test(input.getItem()));
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            navigateTo(entityItem, (pos) -> pickup(entityItem));
            return true;
        }
        return false;
    }


    protected void stashItems(BlockPos chestPos) {
        List<ItemStack> remainingItems = new ArrayList<>();
        TileEntity te = entity.getEntityWorld().getTileEntity(chestPos);
        if (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
            IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
            for (ItemStack stack : entity.getInventory()) {
                ItemStack remaining = ItemHandlerHelper.insertItem(capability, stack, false);
                if (!remaining.isEmpty()) {
                    remainingItems.add(remaining);
                }
            }
            entity.getInventory().clear();
            for (ItemStack item : remainingItems) {
                entity.addStack(item);
            }
        }
        if (!remainingItems.isEmpty()) {
            // Can't do anything
            done();
        }
        needsToPutAway = false;
    }

    protected boolean needToFindChest(boolean lastTask) {
        return needsToPutAway || (lastTask && !entity.isEmptyInventory());
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
                    int cnt = 0;
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
        // Sort so that highest score goes first
        inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
        if (inventories.isEmpty()) {
            return false;
        } else {
            navigateTo(inventories.get(0), job);
            return true;
        }
    }


    protected List<BlockPos> findSuitableInventories(AxisAlignedBB box, Predicate<ItemStack> matcher) {
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
                        if (cnt > 0) {
                            inventories.add(pos);
                            countMatching.put(pos, calculateScore(cnt, free));
                        }
                    }
                });
        // Sort so that highest score goes first
        inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
        return inventories;
    }

    protected List<BlockPos> findInventoriesWithMostSpace(AxisAlignedBB box) {
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
                        int free = 0;
                        for (int i = 0 ; i < handler.getSlots() ; i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (stack.isEmpty()) {
                                free += handler.getSlotLimit(i);
                            }
                        }
                        inventories.add(pos);
                        countMatching.put(pos, (float) free);
                    }
                });
        // Sort so that highest score goes first
        inventories.sort((p1, p2) -> Float.compare(countMatching.get(p2), countMatching.get(p1)));
        return inventories;
    }

}
