package mcjty.meecreeps;

import com.mojang.authlib.GameProfile;
import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.schematics.SchematicHouse;
import mcjty.meecreeps.actions.workers.IdleActionWorker;
import mcjty.meecreeps.actions.workers.WorkerHelper;
import mcjty.meecreeps.api.BuildProgress;
import mcjty.meecreeps.api.IBuildSchematic;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@GameTestHolder("meecreeps")
@PrefixGameTestTemplate(false)
public class BuildingFeedbackTests {
    @GameTest(template = "empty")
    public static void houseReportsEachMaterialShortage(GameTestHelper test) {
        var level = test.getLevel();
        BlockPos target = test.absolutePos(new BlockPos(3, 1, 3));
        var player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "Builder"),
                ClientInformation.createDefault());
        var creep = new EntityMeeCreeps(level);
        var options = new ActionOptions(List.of(), List.of(), target, Direction.UP, level.dimension(), null, 0);
        List<List<String>> messages = new ArrayList<>();
        BlockPos[] nextBlock = {new BlockPos(-3, 1, 0)};
        Set<BlockPos> skipped = new HashSet<>();
        var helper = new WorkerHelper(options) {
            @Override
            protected ServerPlayer getPlayer() {
                return player;
            }

            @Override
            protected void sendMessageToPlayer(ServerPlayer recipient, String message, String... parameters) {
                List<String> sent = new ArrayList<>();
                sent.add(message);
                sent.addAll(List.of(parameters));
                messages.add(sent);
            }

            @Override
            public BlockPos findSpotToBuild(IBuildSchematic schematic, BuildProgress progress, Set<BlockPos> toSkip) {
                return nextBlock[0];
            }

            @Override
            public void navigateTo(BlockPos pos, java.util.function.Consumer<BlockPos> job) {
                job.accept(pos);
            }
        };
        var house = new SchematicHouse(7, helper);
        helper.setWorker(new IdleActionWorker(helper) {
            @Override
            public AABB getSearchBox() {
                return new AABB(target);
            }

            @Override
            public void tick(boolean wrapUp) {
                helper.handleBuilding(house, new BuildProgress(2, 0), skipped);
            }
        });
        Runnable tick = () -> {
            helper.speedUp(0);
            helper.tick(creep, false);
        };

        tick.run();
        tick.run();
        test.assertTrue(messages.equals(List.of(List.of("message.meecreeps.cannot_find", "cobblestone"))),
                "Missing cobblestone was not reported once");

        creep.addStack(new ItemStack(Items.COBBLESTONE));
        tick.run();
        test.assertTrue(level.getBlockState(target.offset(nextBlock[0])).is(net.minecraft.world.level.block.Blocks.COBBLESTONE),
                "Building did not resume after supplying cobblestone");
        tick.run();
        tick.run();
        test.assertTrue(messages.size() == 2 && messages.get(1).equals(List.of("message.meecreeps.cannot_find", "cobblestone")),
                "Running out of supplies again was not reported once");

        nextBlock[0] = new BlockPos(1, 5, 1);
        tick.run();
        tick.run();
        test.assertTrue(messages.size() == 3 && messages.get(2).equals(List.of("message.meecreeps.cannot_find", "glass")),
                "A different missing house material was suppressed");

        nextBlock[0] = new BlockPos(2, 3, 0);
        tick.run();
        test.assertTrue(skipped.contains(nextBlock[0]) && messages.size() == 3,
                "Missing optional house torches should be skipped without a warning");
        test.succeed();
    }
}
