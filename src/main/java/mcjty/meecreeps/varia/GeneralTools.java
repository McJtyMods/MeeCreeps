package mcjty.meecreeps.varia;

import com.mojang.authlib.GameProfile;
import mcjty.lib.varia.DimensionId;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ForgeI18n;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.*;
import java.util.stream.Collectors;

public class GeneralTools {

    private static FakePlayer harvester = null;

    public static FakePlayer getHarvester(World world) {
        if (harvester == null) {
            harvester = FakePlayerFactory.get(DimensionId.overworld().getWorld(), new GameProfile(UUID.nameUUIDFromBytes("meecreeps".getBytes()), "meecreeps"));
        }
        // @todo config, make it possible to specify lesser pickaxe in config
        harvester.setWorld(world);
        harvester.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        return harvester;
    }

    public static List<ITextComponent> createListByNewLine(String i18n, Object... args) {
        return Arrays.stream(ForgeI18n.parseFormat(i18n, args).split("\n"))
                .map(StringTextComponent::new)
                .collect(Collectors.toList());
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

    public static void traverseBox(World world, AxisAlignedBB box, BiPredicate<BlockPos, BlockState> tester, BiConsumer<BlockPos, BlockState> consumer) {
        for (int x = (int) box.minX; x <= box.maxX; x++) {
            for (int y = (int) box.minY; y <= box.maxY; y++) {
                for (int z = (int) box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    if (tester.test(pos, state)) {
                        consumer.accept(pos, state);
                    }
                }
            }
        }
    }
}
