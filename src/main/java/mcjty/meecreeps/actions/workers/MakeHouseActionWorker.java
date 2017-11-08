package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.api.IWorkerHelper;
import mcjty.meecreeps.varia.GeneralTools;
import mcjty.meecreeps.varia.SoundTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MakeHouseActionWorker extends AbstractActionWorker {

    private AxisAlignedBB actionBox = null;
    private int pass = 0;       // Pass when building
    private int stage = 0;      // 0 means flattening. Otherwise it is the level we are currently working on
    private int size = 0;
    // Set of relative positions to skip because they need optional materials
    private Set<BlockPos> toSkip = new HashSet<>();

    public MakeHouseActionWorker(IWorkerHelper helper) {
        super(helper);
    }

    @Override
    public boolean onlyStopWhenDone() {
        return true;
    }

    @Override
    public void init() {
        helper.setSpeed(3);
    }

    @Nullable
    @Override
    public AxisAlignedBB getActionBox() {
        if (actionBox == null) {
            // @todo config
            actionBox = new AxisAlignedBB(options.getTargetPos().add(-12, -5, -12), options.getTargetPos().add(12, 5, 12));
        }
        return actionBox;
    }

    private void placeBuildingBlock(BlockPos pos, IDesiredBlock desiredBlock, boolean jump) {
        World world = entity.getWorld();
        ItemStack blockStack = entity.consumeItem(desiredBlock.getMatcher(), 1);
        if (!blockStack.isEmpty()) {
            if (blockStack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) blockStack.getItem()).getBlock();
                IBlockState stateForPlacement = block.getStateForPlacement(world, pos, EnumFacing.UP, 0, 0, 0, blockStack.getItem().getMetadata(blockStack), GeneralTools.getHarvester(), EnumHand.MAIN_HAND);
                entity.getWorld().setBlockState(pos, stateForPlacement, 3);
                SoundTools.playSound(world, block.getSoundType().getPlaceSound(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
            } else {
                GeneralTools.getHarvester().setPosition(entity.getEntity().posX, entity.getEntity().posY, entity.getEntity().posZ);
                blockStack.getItem().onItemUse(GeneralTools.getHarvester(), world, pos, EnumHand.MAIN_HAND, EnumFacing.UP, 0, 0, 0);
            }
            if (jump) {
                entity.getEntity().getJumpHelper().setJumping();
            }
        }
    }

    private int getSize() {
        if (size == 0) {
            String id = options.getFurtherQuestionId();
            if ("9x9".equals(id)) {
                size = 9;
            } else if ("11x11".equals(id)) {
                size = 11;
            } else {
                size = 13;
            }
        }
        return size;
    }

    private boolean isBorderPos(BlockPos relativePos, int hs) {
        return relativePos.getX() <= -hs || relativePos.getX() >= hs || relativePos.getZ() <= -hs || relativePos.getZ() >= hs;
    }

    private boolean isDoorPos(BlockPos relativePos, int hs) {
        return relativePos.getZ() == 0 && relativePos.getX() == hs;
    }

    private boolean isGlassPos(BlockPos relativePos, int hs) {
        return relativePos.getX() != 0 && relativePos.getZ() != 0 && Math.abs(relativePos.getX()) < (hs - 1) && Math.abs(relativePos.getZ()) < (hs - 1);
    }

    private boolean isTorchPos(BlockPos relativePos, int hs) {
        if (relativePos.getZ() == 0 && (relativePos.getX() == hs - 1 || relativePos.getX() == -hs + 1)) {
            return true;
        }
        return relativePos.getX() == 0 && (relativePos.getZ() == hs - 1 || relativePos.getZ() == -hs + 1);
    }

    private static final IDesiredBlock COBBLE = new IDesiredBlock() {
        @Override
        public String getName() {
            return "cobblestone";
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return stack -> stack.getItem() == Item.getItemFromBlock(Blocks.COBBLESTONE);
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() == Blocks.COBBLESTONE;
        }
    };

    private static final IDesiredBlock GLASS = new IDesiredBlock() {
        @Override
        public String getName() {
            return "glass";
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return stack -> stack.getItem() == Item.getItemFromBlock(Blocks.GLASS);
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() instanceof BlockGlass;
        }
    };

    private static final IDesiredBlock AIR = new IDesiredBlock() {
        @Override
        public String getName() {
            return "air";
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return ItemStack::isEmpty;
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() == Blocks.AIR;
        }
    };

    private static final IDesiredBlock DOOR = new IDesiredBlock() {
        @Override
        public String getName() {
            return "door";
        }

        @Override
        public int getPass() {
            return 1;
        }

        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return stack -> stack.getItem() instanceof ItemDoor;
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() instanceof BlockDoor;
        }
    };

    private static final IDesiredBlock IGNORE = new IDesiredBlock() {
        @Override
        public String getName() {
            return "IGNORE";
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
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> false;
        }
    };

    private static final IDesiredBlock TORCH = new IDesiredBlock() {
        @Override
        public String getName() {
            return "torch";
        }

        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public int getPass() {
            return 1;
        }

        @Override
        public Predicate<ItemStack> getMatcher() {
            return stack -> stack.getItem() == Item.getItemFromBlock(Blocks.TORCH);
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() instanceof BlockTorch;
        }
    };


    private IDesiredBlock getDesiredState(BlockPos relativePos) {
        int hs = (getSize() - 1) / 2;
        switch (relativePos.getY()) {
            case 1:
                if (isDoorPos(relativePos, hs)) {
                    return DOOR;
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return AIR;
                }
            case 2:
                if (isDoorPos(relativePos, hs)) {
                    return IGNORE;
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return AIR;
                }
            case 3:
                if (isTorchPos(relativePos, hs)) {
                    return TORCH;    // @todo
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return AIR;
                }
            case 4:
                if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return AIR;
                }
            case 5:
                if (isGlassPos(relativePos, hs)) {
                    return GLASS;
                } else {
                    return COBBLE;
                }
        }
        return AIR;
    }

    /**
     * Return the relative spot to build
     */
    private BlockPos findSpotToBuild() {
        BlockPos tpos = options.getTargetPos();
        int hs = (getSize() - 1) / 2;

        List<BlockPos> todo = new ArrayList<>();
        for (int x = -hs; x <= hs; x++) {
            for (int z = -hs; z <= hs; z++) {
                BlockPos relativePos = new BlockPos(x, stage, z);
                if (!toSkip.contains(relativePos)) {
                    BlockPos p = tpos.add(relativePos);
                    IBlockState state = entity.getWorld().getBlockState(p);
                    IDesiredBlock desired = getDesiredState(relativePos);
                    if (desired.getPass() == pass && !desired.getStateMatcher().test(state)) {
                        todo.add(relativePos);
                    }
                }
            }
        }
        if (todo.isEmpty()) {
            pass++;
            if (pass >= 2) {
                pass = 0;
                stage++;
                if (stage >= 6) {
                    return null;    // Done
                }
            }
            return findSpotToBuild();
        }
        BlockPos position = entity.getEntity().getPosition().subtract(tpos);        // Make entity position relative for distance calculation
        todo.sort((o1, o2) -> {
            double d1 = position.distanceSq(o1);
            double d2 = position.distanceSq(o2);
            return Double.compare(d1, d2);
        });
        return todo.get(0);
    }

    /**
     * Returns absolute position
     */
    private BlockPos findSpotToFlatten() {
        BlockPos tpos = options.getTargetPos();
        int hs = (getSize() - 1) / 2;

        List<BlockPos> todo = new ArrayList<>();
        for (int x = -hs; x <= hs; x++) {
            for (int y = 1; y <= 5; y++) {
                for (int z = -hs; z <= hs; z++) {
                    BlockPos relativePos = new BlockPos(x, y, z);
                    BlockPos p = tpos.add(relativePos);
                    IBlockState state = entity.getWorld().getBlockState(p);
                    IDesiredBlock desired = getDesiredState(relativePos);
                    if (!desired.getStateMatcher().test(state) && !entity.getWorld().isAirBlock(p)) {
                        todo.add(p);
                    }
                }
            }
        }
        if (todo.isEmpty()) {
            return null;
        }

        BlockPos position = entity.getEntity().getPosition();
        todo.sort((o1, o2) -> {
            double d1 = position.distanceSq(o1);
            double d2 = position.distanceSq(o2);
            return Double.compare(d1, d2);
        });
        return todo.get(0);
    }

    /**
     * Return true if the given postion is air, the postion below is not and the postion above is also air
     */
    private boolean isStandable(BlockPos pos) {
        World world = entity.getWorld();
        return !world.isAirBlock(pos.down()) && world.isAirBlock(pos) && world.isAirBlock(pos.up());
    }

    /**
     * Find the nearest suitable spot to stand on at this x,z
     * Or null if there is no suitable position
     */
    private BlockPos findSuitableSpot(BlockPos pos) {
        if (isStandable(pos)) {
            return pos;
        }
        if (isStandable(pos.down())) {
            return pos.down();
        }
        if (isStandable(pos.up())) {
            return pos.up();
        }
        if (isStandable(pos.down(2))) {
            return pos.down(2);
        }
        return null;
    }

    /**
     * Calculate the best spot to move too for reaching the given position
     */
    private BlockPos findBestNavigationSpot(BlockPos pos) {
        Entity ent = entity.getEntity();
        World world = entity.getWorld();

        BlockPos spotN = findSuitableSpot(pos.north());
        BlockPos spotS = findSuitableSpot(pos.south());
        BlockPos spotW = findSuitableSpot(pos.west());
        BlockPos spotE = findSuitableSpot(pos.east());

        double dn = spotN == null ? Double.MAX_VALUE : spotN.distanceSqToCenter(ent.posX, ent.posY, ent.posZ);
        double ds = spotS == null ? Double.MAX_VALUE : spotS.distanceSqToCenter(ent.posX, ent.posY, ent.posZ);
        double de = spotE == null ? Double.MAX_VALUE : spotE.distanceSqToCenter(ent.posX, ent.posY, ent.posZ);
        double dw = spotW == null ? Double.MAX_VALUE : spotW.distanceSqToCenter(ent.posX, ent.posY, ent.posZ);
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
            if (p != null && !world.isAirBlock(p.up(2))) {
                p = null;
            }
        }

        return p;
    }

    @Override
    public void tick(boolean timeToWrapUp) {
        if (timeToWrapUp) {
            helper.done();
            return;
        }
        if (stage == 0) {
            handleFlatten();
        } else {
            handleBuilding();
        }
    }

    private void handleBuilding() {
        BlockPos relativePos = findSpotToBuild();
        if (relativePos != null) {
            IDesiredBlock desired = getDesiredState(relativePos);
            if (!entity.hasItem(desired.getMatcher())) {
                if (entity.hasRoom(desired.getMatcher())) {
                    if (desired.isOptional()) {
                        if (!helper.findItemOnGroundOrInChest(desired.getMatcher())) {
                            // We don't have any of these. Just skip them
                            toSkip.add(relativePos);
                        }
                    } else {
                        helper.findItemOnGroundOrInChest(desired.getMatcher(), "I cannot find any " + desired.getName());
                    }
                } else {
                    // First put away stuff
                    helper.putStuffAway();
                }
            } else {
                BlockPos buildPos = relativePos.add(options.getTargetPos());
                BlockPos navigate = findBestNavigationSpot(buildPos);
                if (navigate != null) {
                    helper.navigateTo(navigate, p -> {
                        boolean jump = buildPos.up().equals(navigate);
                        placeBuildingBlock(buildPos, desired, jump);
                    });
                } else {
                    // We couldn't reach it. Just build the block
                    placeBuildingBlock(buildPos, desired, false);
                }
            }
        } else {
            helper.taskIsDone();
        }
    }

    private void handleFlatten() {
        BlockPos flatSpot = findSpotToFlatten();
        if (flatSpot == null) {
            stage = 1;
            helper.setSpeed(5);
        } else {
            BlockPos navigate = findBestNavigationSpot(flatSpot);
            if (navigate != null) {
                helper.navigateTo(navigate, p -> helper.harvestAndDrop(flatSpot));
            } else {
                // We couldn't reach it. Just drop the block
                helper.harvestAndDrop(flatSpot);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        stage = tag.getInteger("stage");
        pass = tag.getInteger("pass");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("stage", stage);
        tag.setInteger("pass", pass);
    }
}