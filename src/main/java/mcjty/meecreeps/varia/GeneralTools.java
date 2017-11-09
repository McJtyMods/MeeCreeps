package mcjty.meecreeps.varia;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.*;

public class GeneralTools {

    private static FakePlayer harvester = null;

    public static FakePlayer getHarvester() {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(DimensionManager.getWorld(0), new GameProfile(new UUID(656, 123), "meecreps"));
        }
        // @todo config, make it possible to specify lesser pickaxe in config
        harvester.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        return harvester;
    }

    public static boolean traverseBoxTest(AxisAlignedBB box, Predicate<BlockPos> matcher) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (matcher.test(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static <T> T traverseBoxFirst(AxisAlignedBB box, Function<BlockPos, T> matcher) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    T result = matcher.apply(pos);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static void traverseBoxConsume(AxisAlignedBB box, Consumer<BlockPos> consumer) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    consumer.accept(pos);
                }
            }
        }
    }

    public static void traverseBox(World world, AxisAlignedBB box, BiPredicate<BlockPos, IBlockState> tester, BiConsumer<BlockPos, IBlockState> consumer) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (tester.test(pos, state)) {
                        consumer.accept(pos, state);
                    }
                }
            }
        }
    }
}
