package marc3d.mutils.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

public class CloseWithoutPacket {
    public static void closeScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            client.setScreen(null);
        });
    }

    /**
     * Swaps an item from the Ender Chest with hotbar slot 0 using packets
     * @param slot The slot number in the Ender Chest (0-26 for the 27 slots)
     */
    public static void takeOutItem(int slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || player.currentScreenHandler == null) {
            System.err.println("Player or screen handler is null!");
            return;
        }

        // Validate slot range
        if (slot < 0 || slot > 26) {
            System.err.println("Invalid slot! Must be between 0 and 26.");
            return;
        }

        try {
            int syncId = player.currentScreenHandler.syncId;
            int revision = player.currentScreenHandler.getRevision();

            // Create the packet for SWAP action (pressing number key)
            // button 0 = hotbar slot 0 (key "1")
            ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                syncId,
                revision,
                (short) slot,
                (byte) 0, // button 0 = swap with hotbar slot 0
                SlotActionType.SWAP,
                new Int2ObjectArrayMap<>(),
                ItemStackHash.EMPTY
            );

            player.networkHandler.sendPacket(packet);

        } catch (Exception e) {
            System.err.println("Error sending packet: " + e.getMessage());
        }
    }
}
