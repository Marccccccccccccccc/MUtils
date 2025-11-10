package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.utils.ECUtils;
import marc3d.mutils.utils.movement.Movement;
import marc3d.mutils.utils.SafeSpot;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoEC extends Module {

    public AutoEC() {
        super(MUtils.CATEGORY, "AutoEC", "Teleports you to nearest enderchest and opens it Serverside");
    }

    private List<EnderChestBlockEntity> getAllEnderChestBLockEntity() {
        List<EnderChestBlockEntity> enderchests = new ArrayList<>();

        BlockEntityIterator iterator = new BlockEntityIterator();

        while (iterator.hasNext()) {
            BlockEntity blockEntity = iterator.next();

            if (blockEntity instanceof EnderChestBlockEntity) {
                enderchests.add((EnderChestBlockEntity) blockEntity);
            }
        }
        return enderchests;
    }

    /**
     * Sorts a list of EnderChestBlockEntity by distance from player position.
     * The list is sorted in-place (closest positions first).
     *
     * @param entities The list of EnderChestBlockEntity to sort
     */
    public static void sortByDistance(List<EnderChestBlockEntity> entities) {
        BlockPos playerPos = MeteorClient.mc.player.getBlockPos();

        entities.sort(Comparator.comparingDouble(entity ->
            entity.getPos().getSquaredDistance(playerPos)
        ));
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!ECUtils.getInstance().isEnderChestOpen()) {
            List<EnderChestBlockEntity> enderChests = getAllEnderChestBLockEntity();
            sortByDistance(enderChests);
            Movement.teleport(marc3d.mutils.utils.SafeSpot.findSafeSpot(
                enderChests.get(0).getPos()),
                true,
                false
                );
            ChatUtils.sendMsg("AutoEC", Text.of(String.format("Tried teleporting to safespot %s around ec at %s",
                SafeSpot.findSafeSpot(enderChests.get(0).getPos()),
                enderChests.get(0).getPos())));

        }
    }
}
