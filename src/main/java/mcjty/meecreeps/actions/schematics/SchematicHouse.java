package mcjty.meecreeps.actions.schematics;

import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IDesiredBlock;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class SchematicHouse implements IBuildSchematic {

    private final int size;
    private final IWorkerHelper helper;

    public SchematicHouse(int size, IWorkerHelper helper) {
        this.size = size;
        this.helper = helper;
    }

    @Override
    public BlockPos getMinPos() {
        return new BlockPos(-size/2, 1, -size/2);
    }

    @Override
    public BlockPos getMaxPos() {
        return new BlockPos(size/2, 5, size/2);
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
        public int getAmount() {
            return 128;
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
        public int getAmount() {
            return 64;
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

    private static final IDesiredBlock DOOR = new IDesiredBlock() {
        @Override
        public String getName() {
            return "door";
        }

        @Override
        public int getAmount() {
            return 1;
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

    private static final IDesiredBlock DOORTOP = new IDesiredBlock() {
        @Override
        public String getName() {
            return "door";
        }

        @Override
        public int getAmount() {
            return 0;
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
            return stack -> false;
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> blockState.getBlock() instanceof BlockDoor;
        }
    };

    private static final IDesiredBlock TORCH = new IDesiredBlock() {
        @Override
        public String getName() {
            return "torch";
        }

        @Override
        public int getAmount() {
            return 4;
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
            return stack -> WorkerHelper.isTorch(stack);
        }

        @Override
        public Predicate<IBlockState> getStateMatcher() {
            return blockState -> WorkerHelper.isTorch(blockState.getBlock());
        }
    };


    @Override
    public IDesiredBlock getDesiredBlock(BlockPos relativePos) {
        int hs = (size - 1) / 2;
        IDesiredBlock air = helper.getAirBlock();
        IDesiredBlock ignore = helper.getIgnoreBlock();

        switch (relativePos.getY()) {
            case 1:
                if (isDoorPos(relativePos, hs)) {
                    return DOOR;
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return ignore;
                }
            case 2:
                if (isDoorPos(relativePos, hs)) {
                    return DOORTOP;
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return ignore;
                }
            case 3:
                if (isTorchPos(relativePos, hs)) {
                    return TORCH;    // @todo
                } else if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return ignore;
                }
            case 4:
                if (isBorderPos(relativePos, hs)) {
                    return COBBLE;
                } else {
                    return ignore;
                }
            case 5:
                if (isGlassPos(relativePos, hs)) {
                    return GLASS;
                } else {
                    return COBBLE;
                }
        }
        return air;
    }
}
