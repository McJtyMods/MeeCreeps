package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestReplantActionWorker implements IActionWorker {

    private final ActionOptions options;

    private AxisAlignedBB actionBox = null;
    private BlockPos movingToPos = null;
    private BlockPos movingToPosReplant = null;
    private EntityItem movingToItem = null;
    private boolean needsToPutAway = false;
    private boolean movingToChest = false;
    private int waitABit = 10;

    private Map<BlockPos, Block> needToReplant = new HashMap<>();
    private List<EntityItem> itemsToPickup = new ArrayList<>();

    public HarvestReplantActionWorker(ActionOptions options) {
        this.options = options;
    }

    private AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -5, -10), options.getPos().add(10, 5, 10));
        }
        return actionBox;
    }

    private static boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, entityPlayer)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    private void replant(EntityMeeCreeps entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        Block block = needToReplant.get(pos);
        needToReplant.remove(pos);
        for (ItemStack stack : entity.getInventory()) {
            if (stack.getItem() instanceof IPlantable) {
                IBlockState plant = ((IPlantable) stack.getItem()).getPlant(world, pos);
                if (plant.getBlock() == block) {
                    // This is a valid seed
                    stack.splitStack(1);
                    world.setBlockState(pos, plant);
                    break;
                }
            }
        }
    }

    private void harvest(EntityMeeCreeps entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester());
        entity.getEntityWorld().setBlockToAir(pos);
        boolean replanted = false;
        for (ItemStack stack : drops) {
            if ((!replanted) && stack.getItem() instanceof IPlantable) {
                IBlockState plant = ((IPlantable) stack.getItem()).getPlant(world, pos);
                if (plant.getBlock() == state.getBlock()) {
                    // This is a valid seed
                    ItemStack seed = stack.splitStack(1);
                    world.setBlockState(pos, plant);
                    replanted = true;
                }
            }
            ItemStack remaining = entity.addStack(stack);
            if (!remaining.isEmpty()) {
                EntityItem entityItem = entity.entityDropItem(remaining, 0.0f);
                itemsToPickup.add(entityItem);
                needsToPutAway = true;
            }
        }

        // If we didn't manage to get a seed from the drops we first check if we don't happen to have
        // a seed in our inventory so we can use that.
        for (ItemStack stack : entity.getInventory()) {
            if (stack.getItem() instanceof IPlantable) {
                IBlockState plant = ((IPlantable) stack.getItem()).getPlant(world, pos);
                if (plant.getBlock() == state.getBlock()) {
                    // This is a valid seed
                    ItemStack seed = stack.splitStack(1);
                    world.setBlockState(pos, plant);
                    replanted = true;
                    break;
                }
            }
        }

        if (!replanted) {
            // We could not find any seed at all. Remember this so we can pick a seed from the chest next time
            needToReplant.put(pos, state.getBlock());
        }
    }


    private void pickup(EntityMeeCreeps entity, EntityItem item) {
        ItemStack remaining = entity.addStack(item.getItem().copy());
        if (remaining.isEmpty()) {
            item.setDead();
        } else {
            item.setItem(remaining);
            needsToPutAway = true;
        }
    }

    private boolean hasSuitableSeed(EntityMeeCreeps entity) {
        World world = entity.getEntityWorld();
        for (Map.Entry<BlockPos, Block> entry : needToReplant.entrySet()) {
            BlockPos pos = entry.getKey();
            Block block = entry.getValue();
            for (ItemStack stack : entity.getInventory()) {
                if (stack.getItem() instanceof IPlantable) {
                    IBlockState plant = ((IPlantable) stack.getItem()).getPlant(world, pos);
                    if (plant.getBlock() == block) {
                        // This is a valid seed
                        movingToPosReplant = pos;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void tick(EntityMeeCreeps entity, boolean lastTask) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = 10;

        BlockPos position = entity.getPosition();

        if (movingToPosReplant != null) {
            tryToMoveToPosReplant(entity, position);
        } else if (!needToReplant.isEmpty() && hasSuitableSeed(entity)) {
            // Do nothing
        } else if (needsToPutAway) {
            findChestToPutItemsIn(entity, position);
        } else if (movingToChest) {
            movingToChest = false; // Automatically return to the first state
        } else if (movingToPos != null) {
            tryToMoveToPos(entity, position);
        } else if (movingToItem != null && !movingToItem.isDead) {
            tryToMoveToItem(entity, position);
        } else if (!itemsToPickup.isEmpty()) {
            tryFindingItemsToPickup(entity, position);
        } else if (lastTask) {
            options.setStage(Stage.DONE);
            ServerActionManager.getManager().save();
        } else {
            tryFindingCropsToHarvest(entity, position);
        }
    }

    private void tryFindingCropsToHarvest(EntityMeeCreeps entity, BlockPos position) {
        movingToPos = null;
        AxisAlignedBB box = getActionBox();
        World world = entity.getEntityWorld();
        List<BlockPos> positions = new ArrayList<>();
        for (double x = box.minX ; x <= box.maxX ; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (allowedToHarvest(state, world, pos, GeneralTools.getHarvester())) {
                        if (state.getBlock() == Blocks.FARMLAND) {
                            IBlockState cropState = world.getBlockState(pos.up());
                            Block cropBlock = cropState.getBlock();
                            boolean hasCrops = cropBlock instanceof IPlantable && state.getBlock().canSustainPlant(world.getBlockState(pos), world, pos, EnumFacing.UP, (IPlantable) cropBlock);
                            if (hasCrops) {
                                if (cropBlock instanceof BlockCrops) {
                                    BlockCrops crops = (BlockCrops) cropBlock;
                                    int age = crops.getAge(cropState);
                                    int maxAge = crops.getMaxAge();
                                    if (age >= maxAge) {
                                        positions.add(pos.up());
                                    }
                                } else if (cropBlock instanceof BlockNetherWart) {
                                    BlockNetherWart wart = (BlockNetherWart) cropBlock;
                                    int age = cropState.getValue(BlockNetherWart.AGE);
                                    int maxAge = 3;
                                    if (age >= maxAge) {
                                        positions.add(pos.up());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!positions.isEmpty()) {
            BlockPos cropPos = positions.get(0);
            double d = position.distanceSq(cropPos.getX(), cropPos.getY(), cropPos.getZ());
            if (d < 2) {
                harvest(entity, cropPos);
            } else {
                if (!entity.getNavigator().tryMoveToXYZ(cropPos.getX()+.5, cropPos.getY(), cropPos.getZ()+.5, 2.0)) {
                    // We need to teleport
                    entity.setPositionAndUpdate(cropPos.getX()+.5, cropPos.getY(), cropPos.getZ()+.5);
                }
                movingToPos = cropPos;
            }
        } else if (!entity.getInventory().isEmpty()) {
            needsToPutAway = true;
        }
    }

    private void tryToMoveToPosReplant(EntityMeeCreeps entity, BlockPos position) {
        double d = position.distanceSq(movingToPosReplant);
        if (d < 2) {
            replant(entity, movingToPosReplant);
            movingToPosReplant = null;
        } else if (entity.getNavigator().noPath()) {
            if (!entity.getNavigator().tryMoveToXYZ(movingToPosReplant.getX()+.5, movingToPosReplant.getY(), movingToPosReplant.getZ()+.5, 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(movingToPosReplant.getX()+.5, movingToPosReplant.getY(), movingToPosReplant.getZ()+.5);
            }
        }
    }

    private void tryToMoveToPos(EntityMeeCreeps entity, BlockPos position) {
        double d = position.distanceSq(movingToPos);
        if (d < 2) {
            harvest(entity, movingToPos);
            movingToPos = null;
        } else if (entity.getNavigator().noPath()) {
            if (!entity.getNavigator().tryMoveToXYZ(movingToPos.getX()+.5, movingToPos.getY(), movingToPos.getZ()+.5, 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(movingToPos.getX()+.5, movingToPos.getY(), movingToPos.getZ()+.5);
            }
        }
    }

    private void tryFindingItemsToPickup(EntityMeeCreeps entity, BlockPos position) {
        movingToItem = null;
        List<EntityItem> items = itemsToPickup;
        if (!items.isEmpty()) {
            items.sort((o1, o2) -> {
                double d1 = position.distanceSq(o1.posX, o1.posY, o1.posZ);
                double d2 = position.distanceSq(o2.posX, o2.posY, o2.posZ);
                return Double.compare(d1, d2);
            });
            EntityItem entityItem = items.get(0);
            items.remove(0);
            double d = position.distanceSq(entityItem.posX, entityItem.posY, entityItem.posZ);
            if (d < 2) {
                pickup(entity, entityItem);
            } else {
                if (!entity.getNavigator().tryMoveToEntityLiving(entityItem, 2.0)) {
                    // We need to teleport
                    entity.setPositionAndUpdate(entityItem.posX+.5, entityItem.posY, entityItem.posZ+.5);
                }
                movingToItem = entityItem;
            }
        }
    }

    private void tryToMoveToItem(EntityMeeCreeps entity, BlockPos position) {
        double d = position.distanceSq(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
        if (d < 2) {
            pickup(entity, movingToItem);
            movingToItem = null;
        } else if (entity.getNavigator().noPath()) {
            if (!entity.getNavigator().tryMoveToEntityLiving(movingToItem, 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(movingToItem.posX, movingToItem.posY, movingToItem.posZ);
            }
        }
    }

    private void findChestToPutItemsIn(EntityMeeCreeps entity, BlockPos position) {
        BlockPos pos = options.getPos();
        double d = position.distanceSq(pos);
        if (d < 2) {
            List<ItemStack> remainingItems = new ArrayList<>();
            TileEntity te = entity.getEntityWorld().getTileEntity(pos);
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
                options.setStage(Stage.DONE);
                ServerActionManager.getManager().save();
            }
            needsToPutAway = false;
        } else {
            if (!entity.getNavigator().tryMoveToXYZ(pos.getX()+.5, pos.getY(), pos.getZ()+.5, 2.0)) {
                // We need to teleport
                entity.setPositionAndUpdate(pos.getX()+.5, pos.getY(), pos.getZ()+.5);
            } else {
                movingToChest = true;
            }
        }
    }
}
