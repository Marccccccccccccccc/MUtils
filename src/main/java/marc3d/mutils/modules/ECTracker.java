package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;

public class ECTracker extends Module {

    private boolean wasOpen = false;

    public ECTracker() {
        super(MUtils.CATEGORY, "ECTracker", "Tracks if Ender Chest is open for Starscript");
    }

    @Override
    public void onActivate() {
        updateECStatus();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        updateECStatus();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        // Update immediately when screen opens/closes
        updateECStatus();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        // Server sent close screen packet
        if (event.packet instanceof CloseScreenS2CPacket) {
            if (wasOpen) {
                ChatUtils.warning("Server closed your EC with CloseScreen packet!");
            }
            MeteorStarscript.ss.set("ECOpen", false);
            wasOpen = false;
        }

        // Server sent position packet (also closes screens)
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            if (wasOpen) {
                ChatUtils.warning("Server closed your EC with position packet!");
            }
            MeteorStarscript.ss.set("ECOpen", false);
            wasOpen = false;
        }
    }

    private void updateECStatus() {
        boolean isOpen = isEnderChestOpen();

        // Only log when status changes
        if (isOpen != wasOpen) {
            if (isOpen) {
                ChatUtils.info("Ender Chest opened");
            } else {
                ChatUtils.info("Ender Chest closed");
            }
            wasOpen = isOpen;
        }

        MeteorStarscript.ss.set("ECOpen", isOpen);
    }

    private boolean isEnderChestOpen() {
        if (mc.player == null || mc.player.currentScreenHandler == null) return false;

        // Check if current screen handler is an ender chest (3 rows = 27 slots)
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler) {
            // Additional check: verify the screen title if possible
            if (mc.currentScreen instanceof GenericContainerScreen screen) {
                Text title = screen.getTitle();
                // Ender chests have "Ender Chest" title and 3 rows
                return handler.getRows() == 3 &&
                    (title.getString().equals("Ender Chest") || handler.getInventory().size() == 27);
            }
            // Fallback: just check rows
            return handler.getRows() == 3;
        }

        return false;
    }
}
