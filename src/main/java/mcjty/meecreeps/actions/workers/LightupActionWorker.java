package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.InventoryTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class LightupActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;
    private BlockPos torchChest;

    private AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getPos().add(-10, -5, -10), options.getPos().add(10, 5, 10));
        }
        return actionBox;
    }


    public LightupActionWorker(EntityMeeCreeps entity, ActionOptions options) {
        super(entity, options);
    }

    private BlockPos findDarkSpot() {
        World world = entity.getEntityWorld();
        AxisAlignedBB box = getActionBox();
        return GeneralTools.traverseBoxFirst(box, p -> {
            if (WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, p)) {
                int light = world.getLightFromNeighbors(p);
                if (light < 7) {
                    return p;
                }
            }
            return null;
        });
    }

    private void placeTorch(BlockPos pos) {
        World world = entity.getEntityWorld();
        int light = world.getLightFromNeighbors(pos);
        if (light < 7) {
            ItemStack torch = entity.consumeItem(this::isTorch, 1);
            if (!torch.isEmpty()) {
                entity.getEntityWorld().setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
                SoundTools.playSound(world, Blocks.TORCH.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
            }
        }
    }

    private boolean isTorch(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(Blocks.TORCH);
    }

    private boolean hasTorches() {
        for (ItemStack stack : entity.getInventory()) {
            if (!stack.isEmpty() && isTorch(stack)) {
                return true;
            }
        }
        return false;
    }

    private void fetchTorches(BlockPos pos) {
        TileEntity te = entity.getEntityWorld().getTileEntity(pos);
        torchChest = pos;
        IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
        for (int i = 0 ; i < handler.getSlots() ; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && isTorch(stack)) {
                ItemStack extracted = handler.extractItem(i, stack.getCount(), false);
                ItemStack remaining = entity.addStack(extracted);
                if (!remaining.isEmpty()) {
                    handler.insertItem(i, remaining, false);
                }
            }
        }
    }

    private void putbackTorches(BlockPos pos) {
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

    @Override
    protected void performTick(boolean lastTask) {
        if (lastTask && !entity.isEmptyInventory()) {
            // We need to put back our torches
            if (torchChest == null || !InventoryTools.isInventory(entity.getEntityWorld(), torchChest)) {
                giveToPlayerIfPossible();
            } else {
                navigateTo(torchChest, this::putbackTorches);
            }
        } else if (lastTask) {
            done();
        } else if (!hasTorches()) {
            if (!tryFindingSomeItem(getActionBox(), this::isTorch)) {
                findInventoryContainingMost(getActionBox(), this::isTorch, this::fetchTorches);
            }
        } else {
            BlockPos darkSpot = findDarkSpot();
            if (darkSpot != null) {
                navigateTo(darkSpot, this::placeTorch);
            } else {
                taskIsDone();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("torchChest")) {
            torchChest = BlockPos.fromLong(tag.getLong("torchChest"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        if (torchChest != null) {
            tag.setLong("torchChest", torchChest.toLong());
        }
    }
}