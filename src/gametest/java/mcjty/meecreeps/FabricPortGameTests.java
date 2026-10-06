package mcjty.meecreeps;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
public class FabricPortGameTests {
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void commandpayloadpreservesfieldsandchecksownership(GameTestHelper test) { FabricPortTests.commandPayloadPreservesFieldsAndChecksOwnership(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void housereportseachmaterialshortage(GameTestHelper test) { BuildingFeedbackTests.houseReportsEachMaterialShortage(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 60)
    public void depositsandwithdrawalsanimate(GameTestHelper test) { ChestAnimationTests.depositsAndWithdrawalsAnimate(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 50)
    public void doublechestrefreshesandcloses(GameTestHelper test) { ChestAnimationTests.doubleChestRefreshesAndCloses(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 40)
    public void expirypreservesplayeropening(GameTestHelper test) { ChestAnimationTests.expiryPreservesPlayerOpening(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void componentscopysyncandreciperemainders(GameTestHelper test) { FabricPortTests.componentsCopySyncAndRecipeRemainders(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void harvestingandreplantinguseblockitemseeds(GameTestHelper test) { FabricPortTests.harvestingAndReplantingUseBlockItemSeeds(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void dimensiontransitionpreservescarrieditems(GameTestHelper test) { FabricPortTests.dimensionTransitionPreservesCarriedItems(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 200)
    public void lightingignoresdaylight(GameTestHelper test) { PortGameTests.lightingIgnoresDaylight(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 40)
    public void groundtorchplacement(GameTestHelper test) { PortGameTests.groundTorchPlacement(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void walltorchescompletehouserequirement(GameTestHelper test) { PortGameTests.wallTorchesCompleteHouseRequirement(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void torchplacementdoesnotshiftoccupiedtarget(GameTestHelper test) { PortGameTests.torchPlacementDoesNotShiftOccupiedTarget(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 120)
    public void buildingclearsoccupiedtarget(GameTestHelper test) { PortGameTests.buildingClearsOccupiedTarget(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 120)
    public void buildingmovesasideunderlowceiling(GameTestHelper test) { PortGameTests.buildingMovesAsideUnderLowCeiling(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 120)
    public void buildingclearspartialbodyoverlap(GameTestHelper test) { PortGameTests.buildingClearsPartialBodyOverlap(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 120)
    public void buildingjumpsonnarrowpillar(GameTestHelper test) { PortGameTests.buildingJumpsOnNarrowPillar(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void failedbuildingpreservesmaterials(GameTestHelper test) { PortGameTests.failedBuildingPreservesMaterials(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void energyandcartridgerecipes(GameTestHelper test) { PortGameTests.energyAndCartridgeRecipes(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void actionsandentitypersistence(GameTestHelper test) { PortGameTests.actionsAndEntityPersistence(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void wallportalskeepaimedheight(GameTestHelper test) { PortGameTests.wallPortalsKeepAimedHeight(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 60)
    public void portalpairexpires(GameTestHelper test) { PortGameTests.portalPairExpires(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void harvestinghonorsprotection(GameTestHelper test) { PortGameTests.harvestingHonorsProtection(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void movingblockpreservesinventory(GameTestHelper test) { PortGameTests.movingBlockPreservesInventory(test); }
    @GameTest(structure = "meecreeps:empty", maxTicks = 100)
    public void cartridgeInteractionPrecedesChest(GameTestHelper test) {
        var level = test.getLevel();
        var pos = test.absolutePos(new net.minecraft.core.BlockPos(2, 1, 2));
        level.setBlock(pos, net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), 3);
        var player = new net.minecraft.server.level.ServerPlayer(level.getServer(), level,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Charger"),
                net.minecraft.server.level.ClientInformation.createDefault());
        var cartridge = new net.minecraft.world.item.ItemStack(mcjty.meecreeps.setup.Registration.CARTRIDGE.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, cartridge);
        player.getInventory().setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_PEARL, 2));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        var result = net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(player, level, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        test.assertTrue(result == net.minecraft.world.InteractionResult.SUCCESS, "Cartridge did not consume the chest interaction");
        test.assertTrue(mcjty.meecreeps.items.CartridgeItem.getCharge(cartridge) == mcjty.meecreeps.config.ConfigSetup.chargesPerEnderpearl.get(), "Block interaction did not charge cartridge");
        test.assertTrue(player.getInventory().getItem(1).getCount() == 1, "Charging did not consume exactly one pearl");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
        result = net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.invoker().interact(player, level, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        test.assertTrue(result == net.minecraft.world.InteractionResult.PASS, "Ordinary item blocked chest interaction");
        test.succeed();
    }
}
