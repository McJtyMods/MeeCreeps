package mcjty.meecreeps;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.workers.IdleActionWorker;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.ChestAnimation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder("meecreeps")
@PrefixGameTestTemplate(false)
public class ChestAnimationTests {
    // The dedicated server does not normally tick lids; tick the same controller
    // the client uses to verify that the server block events open and close it.
    private static void animate(ChestBlockEntity chest) {
        ChestBlockEntity.lidAnimateTick(chest.getLevel(), chest.getBlockPos(), chest.getBlockState(), chest);
    }

    @GameTest(template = "empty", timeoutTicks = 60)
    public static void depositsAndWithdrawalsAnimate(GameTestHelper test) {
        var level = test.getLevel();
        BlockPos pos = test.absolutePos(new BlockPos(3, 1, 3));
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        var chest = (ChestBlockEntity) level.getBlockEntity(pos);
        var creep = new EntityMeeCreeps(level);
        creep.addStack(new ItemStack(Items.DIAMOND, 3));
        var options = new ActionOptions(List.of(), List.of(), pos, Direction.UP, level.dimension(), null, 0);
        var helper = new WorkerHelper(options) {
            @Override
            public void navigateTo(BlockPos target, java.util.function.Consumer<BlockPos> job) {
                job.accept(target);
            }
        };
        boolean[] withdraw = {false};
        helper.setWorker(new IdleActionWorker(helper) {
            @Override
            public AABB getSearchBox() {
                return new AABB(pos);
            }

            @Override
            public void tick(boolean wrapUp) {
                if (withdraw[0]) {
                    test.assertTrue(helper.findItemOnGroundOrInChest(stack -> stack.is(Items.DIAMOND), 2),
                            "Could not fetch diamonds from chest");
                } else {
                    helper.putInventoryInChest(pos);
                }
            }
        });
        helper.speedUp(0);
        helper.tick(creep, false);
        test.assertTrue(chest.getItem(0).getCount() == 3, "Deposit did not transfer items");
        test.onEachTick(() -> animate(chest));
        test.runAfterDelay(12, () -> test.assertTrue(chest.getOpenNess(1) > .9F, "Deposit did not open chest"));
        test.runAfterDelay(32, () -> {
            test.assertTrue(chest.getOpenNess(1) == 0, "Chest did not close after deposit");
            withdraw[0] = true;
            helper.speedUp(0);
            helper.tick(creep, false);
            test.assertTrue(chest.getItem(0).getCount() == 1, "Withdrawal did not transfer items");
        });
        test.runAfterDelay(44, () -> {
            test.assertTrue(chest.getOpenNess(1) > .9F, "Withdrawal did not open chest");
            test.assertTrue(ChestBlockEntity.getOpenCount(level, pos) == 0, "Animation changed player opener count");
            test.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 50)
    public static void doubleChestRefreshesAndCloses(GameTestHelper test) {
        var level = test.getLevel();
        BlockPos pos = test.absolutePos(new BlockPos(3, 1, 3));
        var state = Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.LEFT);
        BlockPos otherPos = pos.relative(ChestBlock.getConnectedDirection(state));
        level.setBlock(pos, state, 2);
        level.setBlock(otherPos, state.setValue(ChestBlock.TYPE, ChestType.RIGHT), 2);
        var chest = (ChestBlockEntity) level.getBlockEntity(pos);
        var other = (ChestBlockEntity) level.getBlockEntity(otherPos);
        test.onEachTick(() -> {
            animate(chest);
            animate(other);
        });
        ChestAnimation.open(level, pos);
        test.runAfterDelay(15, () -> ChestAnimation.open(level, otherPos));
        test.runAfterDelay(28, () -> {
            test.assertTrue(chest.getOpenNess(1) > .9F && other.getOpenNess(1) > .9F,
                    "Repeated access did not keep both chest halves open");
        });
        test.runAfterDelay(47, () -> {
            test.assertTrue(chest.getOpenNess(1) == 0 && other.getOpenNess(1) == 0,
                    "Double chest did not close after access");
            test.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void expiryPreservesPlayerOpening(GameTestHelper test) {
        var level = test.getLevel();
        BlockPos pos = test.absolutePos(new BlockPos(3, 1, 3));
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        var chest = (ChestBlockEntity) level.getBlockEntity(pos);
        var player = test.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        test.onEachTick(() -> animate(chest));
        ChestAnimation.open(level, pos);
        test.runAfterDelay(18, () -> chest.startOpen(player));
        test.runAfterDelay(22, () -> {
            test.assertTrue(chest.getOpenNess(1) > .9F, "Animation expiry closed a player-opened chest");
            test.assertTrue(ChestBlockEntity.getOpenCount(level, pos) == 1, "Animation changed player count");
            chest.stopOpen(player);
        });
        test.runAfterDelay(35, () -> {
            test.assertTrue(chest.getOpenNess(1) == 0, "Chest stayed open after player closed it");
            test.succeed();
        });
    }
}
