package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import marc3d.mutils.utils.ECUtils;
import marc3d.mutils.utils.movement.Movement;
import marc3d.mutils.utils.SafeSpot;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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

            // Find first accessible enderchest
            EnderChestBlockEntity validEnderChest = null;
            Vec3d safeSpot = null;

            for (int i = 0; i < enderChests.size(); i++) {
                EnderChestBlockEntity ec = enderChests.get(i);
                safeSpot = SafeSpot.findSafeSpot(ec.getPos());

                if (safeSpot != null) {
                    validEnderChest = ec;
                    break;
                } else {
                    // Remove obstructed enderchest from list
                    enderChests.remove(i);
                    i--; // Adjust index after removal
                    MeteorStarscript.ss.set("AutoECState", "Enderchest obstructed at " + ec.getPos());
                }
            }

            if (validEnderChest == null || safeSpot == null) {
                MeteorStarscript.ss.set("AutoECState", "No accessible enderchests found");
                return;
            }

            Movement.teleport(safeSpot, true, false);
            ChatUtils.sendMsg("AutoEC", Text.of(String.format("Tried teleporting to safespot %s around ec at %s",
                safeSpot,
                validEnderChest.getPos())));

            // Open the enderchest
            BlockPos chestPos = validEnderChest.getPos();
            BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(chestPos),
                Direction.UP,
                chestPos,
                false
            );

            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND,
                hitResult,
                0
            );

            mc.getNetworkHandler().sendPacket(packet);
        }
    }
}
