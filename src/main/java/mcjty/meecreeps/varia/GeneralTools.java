package mcjty.meecreeps.varia;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import org.jspecify.annotations.Nullable;
import java.util.UUID;
import java.util.function.*;

public class GeneralTools {

    public static FakePlayer getHarvester(Level world) {
        FakePlayer harvester = FakePlayerFactory.get((net.minecraft.server.level.ServerLevel) world, new GameProfile(UUID.nameUUIDFromBytes("meecreeps".getBytes(java.nio.charset.StandardCharsets.UTF_8)), "[MeeCreeps]"));
        harvester.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        return harvester;
    }

    public static boolean traverseBoxTest(AABB box, Predicate<BlockPos> matcher) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    if (matcher.test(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public static <T> T traverseBoxFirst(AABB box, Function<BlockPos, T> matcher) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    T result = matcher.apply(pos);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static void traverseBoxConsume(AABB box, Consumer<BlockPos> consumer) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    consumer.accept(pos);
                }
            }
        }
    }

    public static void traverseBox(Level world, AABB box, BiPredicate<BlockPos, BlockState> tester, BiConsumer<BlockPos, BlockState> consumer) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = BlockPos.containing(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (tester.test(pos, state)) {
                        consumer.accept(pos, state);
                    }
                }
            }
        }
    }
}
