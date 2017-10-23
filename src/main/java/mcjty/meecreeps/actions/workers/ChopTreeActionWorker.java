package mcjty.meecreeps.actions.workers;

import mcjty.meecreeps.actions.ActionOptions;
import mcjty.meecreeps.actions.IActionWorker;
import mcjty.meecreeps.actions.ServerActionManager;
import mcjty.meecreeps.actions.Stage;
import mcjty.meecreeps.entities.EntityMeeCreeps;
import mcjty.meecreeps.varia.GeneralTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;

import java.util.List;

public class ChopTreeActionWorker implements IActionWorker {

    private final ActionOptions options;
    private static FakePlayer harvester = null;

    private AxisAlignedBB actionBox = null;
    private int waitABit = 10;

    public ChopTreeActionWorker(ActionOptions options) {
        this.options = options;
    }

    private static boolean allowedToHarvest(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, entityPlayer)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    private void harvest(EntityMeeCreeps entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        List<ItemStack> drops = block.getDrops(world, pos, state, 0);
        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, pos, state, 0, 1.0f, false, GeneralTools.getHarvester());
        entity.getEntityWorld().setBlockToAir(pos);
        for (ItemStack stack : drops) {
            entity.entityDropItem(stack, 0.0f);
        }
    }


    @Override
    public void tick(EntityMeeCreeps entity, boolean lastTask) {
        waitABit--;
        if (waitABit > 0) {
            return;
        }
        // @todo config
        waitABit = 10;

        BlockPos position = entity.getPosition();

        if (lastTask) {
            options.setStage(Stage.DONE);
            ServerActionManager.getManager().save();
        } else {
//            tryFindingItemsToPickup(entity, position);
        }
    }
}
