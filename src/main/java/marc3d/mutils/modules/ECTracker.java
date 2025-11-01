package marc3d.mutils.modules;

import marc3d.mutils.MUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.screen.GenericContainerScreenHandler;

public class ECTracker extends Module {

    public ECTracker() {
        super(MUtils.CATEGORY, "ECTracker", "Tracks if Ender Chest is open for Starscript");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) {
            MeteorStarscript.ss.set("ECOpen", false);
            return;
        }

        boolean isOpen = isEnderChestOpen();
        MeteorStarscript.ss.set("ECOpen", isOpen);
    }

    private boolean isEnderChestOpen() {
        if (mc.player.currentScreenHandler == null) return false;

        // Check if it's a container with 3 rows (27 slots = ender chest)
        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler) {
            return handler.getRows() == 3 && handler.getInventory().size() == 27;
        }

        return false;
    }
}
