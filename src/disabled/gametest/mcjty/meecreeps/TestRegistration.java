package mcjty.meecreeps;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = MeeCreeps.MODID)
public final class TestRegistration {
    private record Test(String name, Consumer<GameTestHelper> function, int ticks) {}
    private static final List<Test> TESTS = List.of(
        new Test("commandpayloadpreservesfieldsandchecksownership", NeoForgePortTests::commandPayloadPreservesFieldsAndChecksOwnership, 100),
        new Test("housereportseachmaterialshortage", BuildingFeedbackTests::houseReportsEachMaterialShortage, 100),
        new Test("depositsandwithdrawalsanimate", ChestAnimationTests::depositsAndWithdrawalsAnimate, 60),
        new Test("doublechestrefreshesandcloses", ChestAnimationTests::doubleChestRefreshesAndCloses, 50),
        new Test("expirypreservesplayeropening", ChestAnimationTests::expiryPreservesPlayerOpening, 40),
        new Test("componentscopysyncandreciperemainders", NeoForgePortTests::componentsCopySyncAndRecipeRemainders, 100),
        new Test("harvestingandreplantinguseblockitemseeds", NeoForgePortTests::harvestingAndReplantingUseBlockItemSeeds, 100),
        new Test("dimensiontransitionpreservescarrieditems", NeoForgePortTests::dimensionTransitionPreservesCarriedItems, 100),
        new Test("lightingignoresdaylight", PortGameTests::lightingIgnoresDaylight, 200),
        new Test("groundtorchplacement", PortGameTests::groundTorchPlacement, 40),
        new Test("walltorchescompletehouserequirement", PortGameTests::wallTorchesCompleteHouseRequirement, 100),
        new Test("torchplacementdoesnotshiftoccupiedtarget", PortGameTests::torchPlacementDoesNotShiftOccupiedTarget, 100),
        new Test("buildingclearsoccupiedtarget", PortGameTests::buildingClearsOccupiedTarget, 120),
        new Test("buildingmovesasideunderlowceiling", PortGameTests::buildingMovesAsideUnderLowCeiling, 120),
        new Test("buildingclearspartialbodyoverlap", PortGameTests::buildingClearsPartialBodyOverlap, 120),
        new Test("buildingjumpsonnarrowpillar", PortGameTests::buildingJumpsOnNarrowPillar, 120),
        new Test("failedbuildingpreservesmaterials", PortGameTests::failedBuildingPreservesMaterials, 100),
        new Test("energyandcartridgerecipes", PortGameTests::energyAndCartridgeRecipes, 100),
        new Test("actionsandentitypersistence", PortGameTests::actionsAndEntityPersistence, 100),
        new Test("wallportalskeepaimedheight", PortGameTests::wallPortalsKeepAimedHeight, 100),
        new Test("portalpairexpires", PortGameTests::portalPairExpires, 60),
        new Test("harvestinghonorsprotection", PortGameTests::harvestingHonorsProtection, 100),
        new Test("movingblockpreservesinventory", PortGameTests::movingBlockPreservesInventory, 100)
    );
    @SubscribeEvent public static void functions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> {
            for (var test : TESTS) helper.register(id(test.name), test.function);
        });
    }
    @SubscribeEvent public static void tests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(id("default"));
        for (var test : TESTS) {
            event.registerTest(id(test.name), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, id(test.name)),
                new TestData<>(environment, id("empty"), test.ticks, 0, true)));
        }
    }
    private static Identifier id(String name) { return Identifier.fromNamespaceAndPath(MeeCreeps.MODID, name); }
}
