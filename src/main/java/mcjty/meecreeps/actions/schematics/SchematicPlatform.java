package mcjty.meecreeps.actions.schematics;

import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.api.IDesiredBlock;
import mcjty.meecreeps.api.IWorkerHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class SchematicPlatform implements IBuildSchematic {

    private final int size;
    private final IWorkerHelper helper;

    public SchematicPlatform(int size, IWorkerHelper helper) {
        this.size = size;
        this.helper = helper;
    }

    @Override
    public BlockPos getMinPos() {
        return new BlockPos(-size/2, 0, -size/2);
    }

    @Override
    public BlockPos getMaxPos() {
        return new BlockPos(size/2, 0, size/2);
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



    @Override
    public IDesiredBlock getDesiredBlock(BlockPos relativePos) {
        return COBBLE;
    }
}
